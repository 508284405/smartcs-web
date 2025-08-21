#!/usr/bin/env python3
import sys
import re
from zipfile import ZipFile
from xml.etree import ElementTree as ET


W_NS = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
R_NS = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
WP_NS = 'http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing'
W_DML_NS = 'http://schemas.openxmlformats.org/drawingml/2006/main'
WPS_NS = 'http://schemas.microsoft.com/office/word/2010/wordprocessingShape'
NS = {'w': W_NS, 'r': R_NS, 'wp': WP_NS, 'a': W_DML_NS, 'wps': WPS_NS}


def qn(tag):
    if ':' in tag:
        prefix, local = tag.split(':', 1)
        if prefix == 'w':
            return f'{{{W_NS}}}{local}'
        if prefix == 'r':
            return f'{{{R_NS}}}{local}'
        if prefix == 'wp':
            return f'{{{WP_NS}}}{local}'
        if prefix == 'a':
            return f'{{{W_DML_NS}}}{local}'
        if prefix == 'wps':
            return f'{{{WPS_NS}}}{local}'
    return tag


def get_attr(elem, qname, default=None):
    # qname like 'w:val'
    try:
        prefix, local = qname.split(':', 1)
    except ValueError:
        return elem.attrib.get(qname, default)
    if prefix == 'w':
        return elem.attrib.get(f'{{{W_NS}}}{local}', default)
    if prefix == 'r':
        return elem.attrib.get(f'{{{R_NS}}}{local}', default)
    return elem.attrib.get(qname, default)


def load_relationships(z):
    rels_map = {}
    try:
        data = z.read('word/_rels/document.xml.rels')
    except KeyError:
        return rels_map
    tree = ET.fromstring(data)
    for rel in tree.findall('.//', namespaces={'rel': 'http://schemas.openxmlformats.org/package/2006/relationships'}):
        # ElementTree loses prefixes, key by Id attribute as-is
        _id = rel.attrib.get('Id') or rel.attrib.get('id')
        target = rel.attrib.get('Target') or rel.attrib.get('target')
        if _id and target:
            rels_map[_id] = target
    return rels_map


def load_numbering(z):
    # Returns two dicts: numId -> abstractNumId; absId -> {lvl: fmt}
    num_to_abs = {}
    abs_lvls = {}
    try:
        data = z.read('word/numbering.xml')
    except KeyError:
        return num_to_abs, abs_lvls
    root = ET.fromstring(data)
    for num in root.findall(f'.//{qn("w:num")}'):
        num_id = get_attr(num, 'w:numId')
        abstract = num.find(f'.//{qn("w:abstractNumId")}')
        if num_id and abstract is not None:
            abs_id = get_attr(abstract, 'w:val')
            num_to_abs[num_id] = abs_id
    for absnum in root.findall(f'.//{qn("w:abstractNum")}'):
        abs_id = get_attr(absnum, 'w:abstractNumId') or get_attr(absnum, 'w:abstractNumId', '')
        if not abs_id:
            continue
        lvls = {}
        for lvl in absnum.findall(f'.//{qn("w:lvl")}'):
            ilvl = get_attr(lvl, 'w:ilvl') or '0'
            fmt = None
            fmt_el = lvl.find(f'.//{qn("w:numFmt")}')
            if fmt_el is not None:
                fmt = get_attr(fmt_el, 'w:val')
            lvls[ilvl] = fmt or 'decimal'
        abs_lvls[abs_id] = lvls
    return num_to_abs, abs_lvls


def extract_text_from_run(r):
    parts = []
    # Tabs and breaks
    for child in r:
        if child.tag == qn('w:t'):
            parts.append(child.text or '')
        elif child.tag == qn('w:tab'):
            parts.append('    ')
        elif child.tag == qn('w:br'):
            parts.append('  \n')  # markdown line break
        elif child.tag == qn('w:drawing'):
            # Try to extract text from text boxes inside drawings
            # Path: w:r/w:drawing//wps:txbx/w:txbxContent//w:p
            for p in child.findall(f'.//{qn("wps:txbx")}//{qn("w:txbxContent")}//{qn("w:p")}'):
                # Reuse paragraph_text but avoid recursion issues by creating a minimal rels map
                parts.append(paragraph_text(p, {}))
    text = ''.join(parts)
    # Apply simple styles
    rpr = r.find(qn('w:rPr'))
    if rpr is not None:
        if rpr.find(qn('w:b')) is not None:
            text = f'**{text}**'
        if rpr.find(qn('w:i')) is not None:
            # avoid double-wrapping bold+italic too messily
            if text.startswith('**') and text.endswith('**'):
                text = f'***{text[2:-2]}***'
            else:
                text = f'*{text}*'
        # ignore underline/strike for markdown simplicity
    return text


def paragraph_text(p, rels):
    texts = []
    for child in p:
        if child.tag == qn('w:r'):
            texts.append(extract_text_from_run2(child))
        elif child.tag == qn('w:hyperlink'):
            # collect display text
            disp = []
            for r in child.findall(qn('w:r')):
                disp.append(extract_text_from_run2(r))
            display = ''.join(disp).strip() or 'link'
            rid = get_attr(child, 'r:id')
            target = rels.get(rid, '') if rid else ''
            if target:
                texts.append(f'[{display}]({target})')
            else:
                texts.append(display)
        # skip other elements in paragraph for simplicity
    s = ''.join(texts).strip()
    if not s:
        # Attempt to extract from any nested textbox content within the paragraph
        for tp in p.findall(f'.//{qn("w:txbxContent")}//{qn("w:p")}'):
            inner = paragraph_text(tp, rels)
            if inner:
                texts.append(inner)
        s = ' '.join(texts).strip()
    return s


def extract_text_from_run2(r):
    # Prefer text from drawings/textboxes to avoid duplication
    drawing_texts = []
    for child in r:
        if child.tag == qn('w:drawing'):
            for p in child.findall(f'.//{qn("wps:txbx")}//{qn("w:txbxContent")}//{qn("w:p")}'):
                drawing_texts.append(paragraph_text(p, {}))
        elif child.tag == qn('w:pict'):
            for p in child.findall(f'.//{qn("w:txbxContent")}//{qn("w:p")}'):
                drawing_texts.append(paragraph_text(p, {}))
    drawing_texts = [t for t in drawing_texts if t]
    if drawing_texts:
        unique = []
        seen = set()
        for t in drawing_texts:
            if t not in seen:
                unique.append(t)
                seen.add(t)
        text = ' '.join(unique)
    else:
        parts = []
        # Tabs and breaks and direct text
        for child in r:
            if child.tag == qn('w:t'):
                parts.append(child.text or '')
            elif child.tag == qn('w:tab'):
                parts.append('    ')
            elif child.tag == qn('w:br'):
                parts.append('  \\n')  # markdown line break
        text = ''.join(parts)
    # Apply simple styles
    rpr = r.find(qn('w:rPr'))
    if rpr is not None:
        if rpr.find(qn('w:b')) is not None:
            text = f'**{text}**'
        if rpr.find(qn('w:i')) is not None:
            # avoid double-wrapping bold+italic too messily
            if text.startswith('**') and text.endswith('**'):
                text = f'***{text[2:-2]}***'
            else:
                text = f'*{text}*'
    return text


def is_list_paragraph(p):
    ppr = p.find(qn('w:pPr'))
    if ppr is None:
        return False
    return ppr.find(qn('w:numPr')) is not None


def list_marker(p, num_to_abs, abs_lvls):
    ppr = p.find(qn('w:pPr'))
    numpr = ppr.find(qn('w:numPr')) if ppr is not None else None
    if numpr is None:
        return '', 0, 'bullet'
    ilvl_el = numpr.find(qn('w:ilvl'))
    numid_el = numpr.find(qn('w:numId'))
    ilvl = get_attr(ilvl_el, 'w:val') if ilvl_el is not None else '0'
    numid = get_attr(numid_el, 'w:val') if numid_el is not None else None
    fmt = 'decimal'
    if numid and numid in num_to_abs:
        abs_id = num_to_abs[numid]
        fmt = abs_lvls.get(abs_id, {}).get(ilvl, 'decimal')
    # map format to markdown marker
    if fmt == 'bullet':
        marker = '-'
        kind = 'bullet'
    else:
        marker = '1.'
        kind = 'number'
    level = int(ilvl) if ilvl and ilvl.isdigit() else 0
    return marker, level, kind


def heading_level(p):
    ppr = p.find(qn('w:pPr'))
    if ppr is None:
        return 0
    style = ppr.find(qn('w:pStyle'))
    if style is None:
        return 0
    val = get_attr(style, 'w:val', '')
    # Typical values: Heading1, Heading2 ... sometimes H1, h1
    m = re.search(r'(?:Heading|H|h)(\d+)$', val)
    if m:
        try:
            return max(1, min(6, int(m.group(1))))
        except ValueError:
            return 0
    return 0


def convert_docx_to_md(docx_path):
    with ZipFile(docx_path) as z:
        rels = load_relationships(z)
        num_to_abs, abs_lvls = load_numbering(z)
        data = z.read('word/document.xml')
        root = ET.fromstring(data)
        body = root.find(qn('w:body'))
        out_lines = []
        def dedup_repeat_line(s: str) -> str:
            tokens = s.split()
            if not tokens:
                return s
            # 1) collapse immediate duplicate tokens
            collapsed = []
            for t in tokens:
                if not collapsed or collapsed[-1] != t:
                    collapsed.append(t)
            # 2) if line is two identical halves, keep one
            n = len(collapsed)
            if n % 2 == 0 and collapsed[: n // 2] == collapsed[n // 2 :]:
                collapsed = collapsed[: n // 2]
            return ' '.join(collapsed)
        for el in body:
            if el.tag == qn('w:p'):
                text = paragraph_text(el, rels)
                if not text:
                    out_lines.append('')
                    continue
                h = heading_level(el)
                if h > 0:
                    out_lines.append(dedup_repeat_line(f"{'#' * h} {text}"))
                elif is_list_paragraph(el):
                    marker, level, kind = list_marker(el, num_to_abs, abs_lvls)
                    indent = '  ' * level
                    out_lines.append(dedup_repeat_line(f"{indent}{marker} {text}"))
                else:
                    out_lines.append(dedup_repeat_line(text))
            elif el.tag == qn('w:tbl'):
                # Simple table extraction: each row -> markdown row with pipes
                rows = el.findall(f'.//{qn("w:tr")}')
                table_lines = []
                for i, tr in enumerate(rows):
                    cells = []
                    for tc in tr.findall(f'.//{qn("w:tc")}'):
                        texts = []
                        for p in tc.findall(f'.//{qn("w:p")}'):
                            t = paragraph_text(p, rels)
                            if t:
                                texts.append(t)
                        cells.append(' '.join(texts).replace('|', '\\|'))
                    row_line = '| ' + ' | '.join(cells) + ' |'
                    table_lines.append(row_line)
                    if i == 0 and cells:
                        table_lines.append('| ' + ' | '.join(['---'] * len(cells)) + ' |')
                if table_lines:
                    out_lines.extend(table_lines)
                    out_lines.append('')
        # Normalize blank lines (no >2 in a row)
        normalized = []
        blank_count = 0
        for line in out_lines:
            if line.strip() == '':
                blank_count += 1
            else:
                blank_count = 0
            if blank_count <= 2:
                normalized.append(line.rstrip())
        return '\n'.join(normalized).rstrip() + '\n'


def main():
    if len(sys.argv) < 3:
        print('Usage: docx_to_md.py <input.docx> <output.md>')
        sys.exit(1)
    input_path = sys.argv[1]
    output_path = sys.argv[2]
    md = convert_docx_to_md(input_path)
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(md)


if __name__ == '__main__':
    main()
