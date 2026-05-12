from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "docs" / "assets"
OUTPUT = ROOT / "docs" / "final-demo-console-output.txt"
SQL_DEMO = ROOT / "sql" / "demo" / "insert_demo_data.sql"


def font(size=22, bold=False):
    candidates = [
        "C:/Windows/Fonts/consola.ttf",
        "C:/Windows/Fonts/consolab.ttf" if bold else "C:/Windows/Fonts/consola.ttf",
        "C:/Windows/Fonts/cour.ttf",
    ]

    for path in candidates:
        if Path(path).exists():
            return ImageFont.truetype(path, size=size)

    return ImageFont.load_default()


def write_panel(path, title, lines, width=1500, height=930):
    image = Image.new("RGB", (width, height), "#ffffff")
    draw = ImageDraw.Draw(image)
    title_font = font(28, bold=True)
    body_font = font(20)

    draw.rectangle((0, 0, width, 72), fill="#16324f")
    draw.text((28, 20), title, fill="#ffffff", font=title_font)

    y = 95
    for line in lines:
        if y > height - 34:
            draw.text((28, y), "...", fill="#111111", font=body_font)
            break
        draw.text((28, y), line[:150], fill="#111111", font=body_font)
        y += 28

    image.save(path)


def clean_lines(text):
    return [
        line.rstrip()
        for line in text.splitlines()
        if not line.startswith("[INFO]") and not line.startswith("mvn :")
    ]


def between(lines, start_marker, end_marker=None, max_lines=28):
    start = next(i for i, line in enumerate(lines) if start_marker in line)
    if end_marker:
        end = next(i for i, line in enumerate(lines[start + 1 :], start + 1) if end_marker in line)
    else:
        end = min(len(lines), start + max_lines)
    return lines[start:end][:max_lines]


def main():
    ASSETS.mkdir(parents=True, exist_ok=True)
    try:
        output_text = OUTPUT.read_text(encoding="utf-16")
    except UnicodeError:
        output_text = OUTPUT.read_text(encoding="utf-8", errors="ignore")

    lines = clean_lines(output_text)

    write_panel(
        ASSETS / "final-demo-document-base.png",
        "Console evidence: main starts the controlled 10-document LSI flow",
        between(lines, "FINAL PROJECT TECHNICAL DEMONSTRATION", "STEP 3. FREQUENCY MATRIX FrecT", 36),
    )

    write_panel(
        ASSETS / "final-demo-lsi-queries.png",
        "Console evidence: FrecT, SVD/LSI, selected terms, and query outputs",
        between(lines, "STEP 3. FREQUENCY MATRIX FrecT", "STEP 8. SQL VALIDATION AND PERSISTENCE", 52),
        height=1120,
    )

    sql_lines = SQL_DEMO.read_text(encoding="utf-8").splitlines()
    interesting = []
    for line in sql_lines:
        if "('D" in line or "INSERT INTO documents" in line or "INSERT INTO selected_index_terms" in line:
            interesting.append(line.rstrip())

    write_panel(
        ASSETS / "sql-demo-d1-d10.png",
        "SQL evidence: relational demo data aligns with D1-D10",
        interesting[:32],
        height=900,
    )


if __name__ == "__main__":
    main()
