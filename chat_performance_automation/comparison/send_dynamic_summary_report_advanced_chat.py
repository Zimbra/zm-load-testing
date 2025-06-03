import os
import csv
import smtplib
from email.message import EmailMessage
from dotenv import load_dotenv

load_dotenv()

CONCURRENT_USERS = int(os.getenv("CONCURRENT_USERS", "0"))
TEST_DURATION_MINUTES = int(os.getenv("TEST_DURATION_MINUTES", "0"))

CSV_PATH = "/tmp/Chat/results/transaction_response_times_comparison_advanced_chat.csv"
HTML_REPORT_PATH = "results_report_advanced_chat.html"

def parse_summary(csv_path):
    improved = regressed = negligible = 0
    total_resp_v1 = total_resp_v2 = change_pct = 0.0

    with open(csv_path, newline='', encoding='utf-8') as csvfile:
        reader = list(csv.DictReader(csvfile))
        for row in reader:
            if row["Label"].lower() == "total":
                total_resp_v1 = float(row["Resp Time for v10.1.7"])
                total_resp_v2 = float(row["Resp Time for v10.1.8"])
                change_pct = float(row["Degradation (%)"])
            else:
                color = row["Color"].lower()
                if "green" in color:
                    improved += 1
                elif "red" in color:
                    regressed += 1
                else:
                    negligible += 1

    return {
        "resp_v1": total_resp_v1,
        "resp_v2": total_resp_v2,
        "change_pct": change_pct,
        "improved": improved,
        "regressed": regressed,
        "negligible": negligible
    }

def create_summary_html(summary,users,duration):
 return f"""
    <p>Hi Team,</p>
    <p>Please find below a summary of the latest performance test results for advanced Chat:</p>

    <p><strong>Test Configuration:</strong></p>
    <ul>
        <li>Concurrent Users: <strong>{users}</strong></li>
        <li>Test Duration: <strong>{duration} minutes</strong></li>
    </ul>

    <table border="1" cellpadding="6" cellspacing="0" style="border-collapse: collapse; font-family: Arial; font-size: 14px;">
        <thead style="background-color: #1976d2; color: white;">
            <tr>
                <th>Metric</th>
                <th>v10.1.7</th>
                <th>v10.1.8</th>
                <th>Change</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td><strong>Total Avg Response Time (ms)</strong></td>
                <td>{summary["resp_v1"]:.0f}</td>
                <td>{summary["resp_v2"]:.0f}</td>
                <td style="color: {'red' if summary['change_pct'] > 0 else 'green'};">{summary["change_pct"]:+.2f}%</td>
            </tr>
            <tr>
                <td><strong>Transactions Improved</strong></td>
                <td colspan="3" style="color: green;">{summary["improved"]}</td>
            </tr>
            <tr>
                <td><strong>Transactions Regressed</strong></td>
                <td colspan="3" style="color: red;">{summary["regressed"]}</td>
            </tr>
            <tr>
                <td><strong>Moderate Degradation</strong></td>
                <td colspan="3">{summary["negligible"]}</td>
            </tr>
        </tbody>
    </table>
    """
def send_email_report(summary_html, full_report_html):
    msg = EmailMessage()
    msg['Subject'] = 'Automated advanced Chat Performance Test Report'
    msg['From'] = os.getenv("EMAIL_USER")
    msg['To'] = os.getenv("EMAIL_TO")

    msg.set_content("This email contains a summary and an interactive HTML report.")
    msg.add_alternative(summary_html + full_report_html, subtype='html')

    host = os.getenv("EMAIL_HOST")
    port = int(os.getenv("EMAIL_PORT"))
    username = os.getenv("EMAIL_USER")
    password = os.getenv("EMAIL_PASS")

    with smtplib.SMTP(host, port) as server:
        server.starttls()
        server.login(username, password)
        server.send_message(msg)

    print("✅ Email sent successfully.")

if __name__ == "__main__":
    summary_data = parse_summary(CSV_PATH)

    with open(HTML_REPORT_PATH, 'r', encoding='utf-8') as f:
        full_report = f.read()

    summary_html = create_summary_html(summary_data,CONCURRENT_USERS,TEST_DURATION_MINUTES)
    send_email_report(summary_html, full_report)
