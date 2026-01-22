from pathlib import Path
import textwrap

base = Path(r"C:\Users\PC-HP\Desktop\ahmed\mcptest\S5\rapport S5")
text_path = base / "Chapter3_Implementation_Report.txt"
pdf_path = base / "Chapter3_Implementation_Report.pdf"

text = text_path.read_text(encoding="ascii", errors="replace")
lines = text.splitlines()

wrapped_lines = []
width = 72
for line in lines:
    if not line.strip():
        wrapped_lines.append("")
        continue
    stripped = line.strip()
    if stripped.startswith("- "):
        content = stripped[2:]
        parts = textwrap.wrap(content, width=width-2) or [""]
        for i, part in enumerate(parts):
            if i == 0:
                wrapped_lines.append("- " + part)
            else:
                wrapped_lines.append("  " + part)
    else:
        parts = textwrap.wrap(line, width=width) or [""]
        wrapped_lines.extend(parts)

page_width = 595
page_height = 842
margin = 72
font_size = 12
line_height = 14
max_lines = int((page_height - 2 * margin) / line_height)

pages = []
for i in range(0, len(wrapped_lines), max_lines):
    pages.append(wrapped_lines[i:i + max_lines])

def escape_pdf(s: str) -> str:
    s = s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
    return s

objects = []

def add_obj(content: str) -> int:
    objects.append(content)
    return len(objects)

# Font object
font_obj_num = add_obj("<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>")

page_objs = []
content_objs = []

for page_lines in pages:
    # Build content stream
    start_x = margin
    start_y = page_height - margin - font_size
    text_lines = []
    text_lines.append("BT")
    text_lines.append(f"/F1 {font_size} Tf")
    text_lines.append(f"{start_x} {start_y} Td")
    for line in page_lines:
        safe = escape_pdf(line)
        text_lines.append(f"({safe}) Tj")
        text_lines.append(f"0 -{line_height} Td")
    text_lines.append("ET")
    stream = "\n".join(text_lines)
    stream_bytes = stream.encode("ascii", errors="replace")
    content_obj = f"<< /Length {len(stream_bytes)} >>\nstream\n{stream}\nendstream"
    content_num = add_obj(content_obj)
    content_objs.append(content_num)

# Pages and page objects need references
# Create page objects now
pages_kids = []
for idx, content_num in enumerate(content_objs):
    page_obj = (
        "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
        f"/Resources << /Font << /F1 {font_obj_num} 0 R >> >> "
        f"/Contents {content_num} 0 R >>"
    )
    page_num = add_obj(page_obj)
    page_objs.append(page_num)
    pages_kids.append(f"{page_num} 0 R")

pages_obj = f"<< /Type /Pages /Kids [ {' '.join(pages_kids)} ] /Count {len(page_objs)} >>"
# Insert Pages object as object 2
# Move existing objects to make sure Pages is object 2
# Current objects: 1=Font, 2..=Content+Page objects. We will rebuild properly.

# Rebuild with Pages as object 2
final_objects = []
final_objects.append(objects[0])
final_objects.append(pages_obj)
# Append all content and page objects after
final_objects.extend(objects[1:])

# Catalog object
catalog_obj = "<< /Type /Catalog /Pages 2 0 R >>"
final_objects.append(catalog_obj)

# Build PDF
xref_positions = []
result = []
result.append("%PDF-1.4\n")

byte_count = len(result[0].encode("ascii"))

for i, obj in enumerate(final_objects, start=1):
    xref_positions.append(byte_count)
    obj_str = f"{i} 0 obj\n{obj}\nendobj\n"
    result.append(obj_str)
    byte_count += len(obj_str.encode("ascii"))

xref_start = byte_count
xref = ["xref\n", f"0 {len(final_objects) + 1}\n", "0000000000 65535 f \n"]
for pos in xref_positions:
    xref.append(f"{pos:010d} 00000 n \n")
result.append("".join(xref))
result.append("trailer\n")
result.append(f"<< /Size {len(final_objects) + 1} /Root {len(final_objects)} 0 R >>\n")
result.append("startxref\n")
result.append(f"{xref_start}\n")
result.append("%%EOF\n")

pdf_data = "".join(result).encode("ascii", errors="replace")
pdf_path.write_bytes(pdf_data)
print(str(pdf_path))
