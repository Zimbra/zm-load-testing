import os
import pandas as pd
import smtplib
from email.message import EmailMessage
from dotenv import load_dotenv

load_dotenv()

CONCURRENT_USERS = int(os.getenv("CONCURRENT_USERS", "0"))
TEST_DURATION_MINUTES = int(os.getenv("TEST_DURATION_MINUTES", "0"))
test_path = os.getenv("test_path")

CSV_PATH = f"{test_path}/advanced_chat_build_comparison/generic-advanced-chat-requests_standalone.csv"

def csv_to_html_table(csv_path):
    df = pd.read_csv(csv_path)
    return df.to_html(index=False, border=0, justify='center', classes='data-table')

def create_summary_html(users, duration):
    return f"""
    <html>
    <head>
      <style>
        body {{
          font-family: Arial, sans-serif;
          color: #333;
          margin: 0;
          padding: 0 20px;
        }}
        .data-table {{
          border-collapse: collapse;
          width: 100%;
          max-width: 900px;
          margin-top: 20px;
          cursor: default;
          table-layout: auto; /* allow dynamic widths */
        }}
        .data-table th, .data-table td {{
          border: 1px solid #ddd;
          padding: 10px;
          user-select: none;
          font-size: 14px;
          text-align: center;
        }}
        /* Left align and widen first column (Label) */
        .data-table th:first-child,
        .data-table td:first-child {{
          text-align: left;
          width: 25%;
          white-space: nowrap;
          padding-left: 15px;
        }}
        .data-table th {{
          background-color: #4CAF50;
          color: white;
          position: sticky;
          top: 0;
          z-index: 1;
        }}
        .data-table tr:nth-child(even) {{
          background-color: #f9f9f9;
        }}
        .data-table tr:hover {{
          background-color: #d1e7dd;
        }}
        p.note {{
          background-color: #fff3cd;
          border-left: 6px solid #ffeeba;
          padding: 10px;
          font-style: italic;
          color: #856404;
          max-width: 600px;
        }}
        h3 {{
          margin-top: 40px;
          color: #4CAF50;
        }}
      </style>
    </head>
    <body>
      <p>Hi Team,</p>
      <p>Please find below a summary of the latest performance test results for advanced Chat:</p>

      <p><strong>Test Configuration:</strong></p>
      <ul>
          <li>Concurrent Users: <strong>{users}</strong></li>
          <li>Test Duration: <strong>{duration} minutes</strong></li>
      </ul>

      <p class="note"><strong>Note:</strong> All response times in the table below are in <strong>milliseconds (ms)</strong>.</p>

      <h3>📊 Test Results:</h3>
    """

def send_email_report(summary_html, table_html):
    msg = EmailMessage()
    msg['Subject'] = 'Automated Standalone advanced Chat Performance Test Report'
    msg['From'] = os.getenv("EMAIL_USER")
    msg['To'] = os.getenv("EMAIL_TO")

    msg.set_content("This email contains a summary and performance metrics in HTML format.")

    full_html = summary_html + table_html + "</body></html>"

    msg.add_alternative(full_html, subtype='html')

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
    summary_html = create_summary_html(CONCURRENT_USERS, TEST_DURATION_MINUTES)
    table_html = csv_to_html_table(CSV_PATH)
    send_email_report(summary_html, table_html)
