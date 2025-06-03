import csv
from pathlib import Path

# Map color names to background colors (lighter shades for better readability)
COLOR_MAP_BG = {
    "Dark Green": "#c8e6c9",  
    "Green": "#dcedc8",       
    "Red": "#ffcdd2",         
    "Orange": "#ffe0b2"       
}

def generate_html_report_from_csv(csv_path, output_html_path):
    rows = []
    with open(csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            rows.append(row)

    html = f"""
    <html>
    <head>
        <title>Performance Comparison Report - advanced Chat</title>
        <style>
            body {{
                font-family: Arial, sans-serif;
                margin: 20px;
                background: #fafafa;
            }}
            table {{
                border-collapse: collapse;
                width: 100%;
                box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                border-radius: 8px;
                overflow: hidden;
            }}
            th, td {{
                padding: 12px 15px;
                text-align: center;
                border-bottom: 1px solid #ddd;
            }}
            th {{
                background-color: #1976d2;
                color: white;
                text-transform: uppercase;
                letter-spacing: 0.05em;
            }}
            tr:hover {{
                background-color: #f1f1f1;
            }}
            tr.status-darkgreen {{
                background-color: {COLOR_MAP_BG['Dark Green']};
                font-weight: bold;
                color: #2e7d32;
            }}
            tr.status-green {{
                background-color: {COLOR_MAP_BG['Green']};
                font-weight: bold;
                color: #8FBC8F;
            }}
            tr.status-red {{
                background-color: {COLOR_MAP_BG['Red']};
                font-weight: bold;
                color: #c62828;
            }}
            tr.status-orange {{
                background-color: {COLOR_MAP_BG['Orange']};
                font-weight: bold;
                color: #FFA500;
            }}
            .legend {{
                margin-top: 20px;
                font-size: 0.9em;
            }}
            .legend span {{
                display: inline-block;
                padding: 5px 12px;
                margin-right: 10px;
                border-radius: 5px;
                font-weight: bold;
                color: white;
            }}
            .legend .darkgreen {{ background-color: #2e7d32; }}
            .legend .green {{ background-color: #8FBC8F; }}
            .legend .red {{ background-color: #c62828; }}
            .legend .orange {{ background-color: #ef6c00; }}
        </style>
    </head>
    <body>
        <h2>Performance Comparison Report - advanced Chat</h2>
        <table>
            <thead>
                <tr>
                    <th>Label</th>
                    <th>Resp Time for v10.1.7 (ms)</th>
                    <th>Resp Time for v10.1.8 (ms)</th>
                    <th>Degradation (%)</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
    """

    # Helper to map color name to CSS class suffix
    def color_to_class(color_name):
        mapping = {
            "Dark Green": "darkgreen",
            "Green": "green",
            "Red": "red",
            "Orange": "orange"
        }
        return mapping.get(color_name, "")

    for row in rows:
        color_name = row["Color"].strip()
        css_class = f"status-{color_to_class(color_name)}"
        html += f"""
            <tr class="{css_class}">
                <td>{row['Label']}</td>
                <td>{float(row['Resp Time for v10.1.7']):,.2f}</td>
                <td>{float(row['Resp Time for v10.1.8']):,.2f}</td>
                <td>{float(row['Degradation (%)']):,.2f}%</td>
                <td>{color_name}</td>
            </tr>
        """

    html += """
            </tbody>
        </table>

        <div class="legend">
            <span class="darkgreen">Dark Green = Improved</span>
            <span class="green">Green = Good</span>
            <span class="red">Red = Regressed</span>
            <span class="orange">Orange = Moderate degradation</span>
        </div>
    </body>
    </html>
    """

    with open(output_html_path, 'w', encoding='utf-8') as f:
        f.write(html)
    print(f"✅ HTML report generated: {output_html_path}")


if __name__ == "__main__":
    csv_file_path = "/tmp/Chat/results/transaction_response_times_comparison_advanced_chat.csv"            # Change to your CSV file path
    output_html_file = "results_report_advanced_chat.html" # Output HTML file name

    generate_html_report_from_csv(csv_file_path, output_html_file)
