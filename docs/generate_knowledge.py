#!/usr/bin/env python3
"""
Java Knowledge HTML Generator
Parse Java files and generate HTML pages with Amber & Cyan Tech style
"""

import os
import re
import json
from pathlib import Path
from dataclasses import dataclass
from typing import List, Optional

@dataclass
class KnowledgePoint:
    """Represents a knowledge point"""
    file_path: str
    package: str
    class_name: str
    title: str
    description: str
    sections: List[dict]
    code_content: str


def extract_package(java_content: str) -> str:
    """Extract package declaration"""
    match = re.search(r'package\s+([\w.]+);', java_content)
    return match.group(1) if match else ""


def extract_class_name(java_content: str) -> str:
    """Extract public class name"""
    match = re.search(r'public\s+(?:class|enum|interface)\s+(\w+)', java_content)
    return match.group(1) if match else ""


def extract_javadoc(java_content: str) -> tuple:
    """Extract class-level Javadoc comment"""
    # Match class Javadoc (before package or before class)
    pattern = r'/\*\*(.*?)\*/'
    matches = re.findall(pattern, java_content, re.DOTALL)

    if matches:
        # Usually the first one is the class Javadoc
        javadoc = matches[0]
        # Clean up the comment
        lines = [line.strip().lstrip('*').strip() for line in javadoc.split('\n')]
        return '\n'.join(lines)
    return ""


def parse_sections(java_content: str) -> List[dict]:
    """Parse Java content into sections"""
    sections = []

    # Extract the main Javadoc as overview
    javadoc = extract_javadoc(java_content)
    if javadoc:
        sections.append({
            'type': 'overview',
            'title': '核心概念',
            'content': javadoc
        })

    # Find all block comments that look like sections
    section_pattern = r'//\s*=+\s*(.+?)\s*=+\s*\n(.*?)(?=//\s*=+|\Z)'
    for match in re.finditer(section_pattern, java_content, re.DOTALL):
        title = match.group(1).strip()
        content = match.group(2).strip()

        # Determine section type
        if 'public static void main' in content:
            sec_type = 'demo'
        elif any(kw in title.lower() for kw in ['实现', '方式', '模式']):
            sec_type = 'implementation'
        elif any(kw in title.lower() for kw in ['测试', 'demo', 'main']):
            sec_type = 'demo'
        else:
            sec_type = 'concept'

        sections.append({
            'type': sec_type,
            'title': title,
            'content': content
        })

    return sections


def extract_code_sections(java_content: str) -> List[dict]:
    """Extract code sections from Java file"""
    code_sections = []

    # Find all static inner classes
    inner_class_pattern = r'(static\s+(?:class|enum|interface)\s+\w+\s*(?:<[^>]+>)?\s*\{[\s\S]*?\n\s*\})'
    for i, match in enumerate(re.finditer(inner_class_pattern, java_content)):
        code = match.group(1)
        # Try to find preceding comment
        start_pos = match.start()
        comment = ""
        if start_pos > 0:
            before = java_content[:start_pos]
            comment_match = re.search(r'/\*\*(.*?)\*/\s*$', before, re.DOTALL)
            if comment_match:
                comment = comment_match.group(1).strip()

        code_sections.append({
            'type': 'class',
            'comment': comment,
            'code': code
        })

    # Find main method
    main_pattern = r'(public\s+static\s+void\s+main\s*\([^)]+\)\s*\{[\s\S]*?\n\s*\})'
    main_match = re.search(main_pattern, java_content)
    if main_match:
        code_sections.append({
            'type': 'main',
            'comment': '测试代码',
            'code': main_match.group(1)
        })

    return code_sections


def escape_html(text: str) -> str:
    """Escape HTML special characters"""
    return (text
            .replace('&', '&amp;')
            .replace('<', '&lt;')
            .replace('>', '&gt;')
            .replace('"', '&quot;'))


def format_content(content: str) -> str:
    """Format content with proper HTML"""
    lines = content.split('\n')
    result = []
    in_code_block = False
    code_buffer = []

    for line in lines:
        stripped = line.strip()

        # Check for code blocks (indented or marked with /* */
        if stripped.startswith('/*') and not stripped.startswith('/**'):
            in_code_block = True
            code_buffer = [line]
        elif in_code_block:
            code_buffer.append(line)
            if '*/' in stripped:
                in_code_block = False
                # Extract code from block comment
                code_text = '\n'.join(code_buffer)
                code_text = re.sub(r'/\*+\s*\n?', '', code_text)
                code_text = re.sub(r'\n?\s*\*+/', '', code_text)
                code_text = re.sub(r'^\s*\*\s?', '', code_text, flags=re.MULTILINE)
                result.append(f'<pre><code>{escape_html(code_text.strip())}</code></pre>')
                code_buffer = []
        elif stripped.startswith('```'):
            if in_code_block:
                in_code_block = False
                code_text = '\n'.join(code_buffer)
                result.append(f'<pre><code>{escape_html(code_text)}</code></pre>')
                code_buffer = []
            else:
                in_code_block = True
                code_buffer = []
        elif in_code_block:
            code_buffer.append(line)
        elif stripped.startswith('|') and '|' in stripped[1:]:
            # Table row
            result.append(format_table_line(line))
        elif re.match(r'^[\s]*[\-\+\|]', stripped):
            # ASCII table/art
            result.append(f'<div class="ascii-art">{escape_html(line)}</div>')
        elif re.match(r'^【', stripped):
            # Section header in Chinese brackets
            result.append(f'<h3>{escape_html(stripped)}</h3>')
        elif stripped.startswith('1.') or stripped.startswith('2.') or stripped.startswith('- '):
            # List item
            result.append(f'<li>{escape_html(stripped[2:].strip() if stripped.startswith("- ") else stripped)}</li>')
        else:
            result.append(f'<p>{escape_html(line)}</p>')

    return '\n'.join(result)


def format_table_line(line: str) -> str:
    """Format a table line"""
    cells = [cell.strip() for cell in line.split('|') if cell.strip()]
    if all(c.replace('-', '').replace('=', '').strip() == '' for c in cells):
        return ''  # Separator line
    return '<tr>' + ''.join(f'<td>{escape_html(c)}</td>' for c in cells) + '</tr>'


def generate_html(kp: KnowledgePoint, template: str) -> str:
    """Generate HTML from knowledge point using template"""

    # Format sections
    sections_html = []
    for section in kp.sections:
        content = format_content(section['content'])
        sections_html.append(f'''
        <section class="kp-section kp-section-{section['type']}">
            <h2 class="section-title">{escape_html(section['title'])}</h2>
            <div class="section-content">{content}</div>
        </section>
        ''')

    # Format code
    code_html = []
    for code_sec in extract_code_sections(kp.code_content):
        code_html.append(f'''
        <div class="code-block">
            <div class="code-header">
                <span class="code-title">{escape_html(code_sec['comment'] or 'Code')}</span>
                <button class="copy-btn" onclick="copyCode(this)">Copy</button>
            </div>
            <pre><code class="language-java">{escape_html(code_sec['code'])}</code></pre>
        </div>
        ''')

    # Replace template variables
    html = template
    html = html.replace('{{TITLE}}', kp.title or kp.class_name)
    html = html.replace('{{CLASS_NAME}}', kp.class_name)
    html = html.replace('{{PACKAGE}}', kp.package)
    html = html.replace('{{DESCRIPTION}}', kp.description or 'Java Knowledge Point')
    html = html.replace('{{SECTIONS}}', '\n'.join(sections_html))
    html = html.replace('{{CODE_SECTIONS}}', '\n'.join(code_html))

    return html


def parse_java_file(file_path: str) -> KnowledgePoint:
    """Parse a Java file and extract knowledge"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    package = extract_package(content)
    class_name = extract_class_name(content)
    javadoc = extract_javadoc(content)

    # Extract title from first line of Javadoc
    title = class_name
    description = ""
    if javadoc:
        lines = [l.strip() for l in javadoc.split('\n') if l.strip()]
        if lines:
            title = lines[0]
            description = '\n'.join(lines[1:]) if len(lines) > 1 else ""

    sections = parse_sections(content)

    return KnowledgePoint(
        file_path=file_path,
        package=package,
        class_name=class_name,
        title=title,
        description=description,
        sections=sections,
        code_content=content
    )


def load_template(template_path: str) -> str:
    """Load HTML template"""
    with open(template_path, 'r', encoding='utf-8') as f:
        return f.read()


def main():
    """Main entry point"""
    import argparse

    parser = argparse.ArgumentParser(description='Generate HTML from Java knowledge files')
    parser.add_argument('--template', required=True, help='HTML template file path')
    parser.add_argument('--input', required=True, help='Input Java file or directory')
    parser.add_argument('--output', required=True, help='Output directory for HTML files')

    args = parser.parse_args()

    # Load template
    template = load_template(args.template)

    # Process files
    input_path = Path(args.input)
    output_path = Path(args.output)
    output_path.mkdir(parents=True, exist_ok=True)

    if input_path.is_file():
        java_files = [input_path]
    else:
        java_files = list(input_path.rglob('*.java'))

    for java_file in java_files:
        try:
            kp = parse_java_file(str(java_file))
            html = generate_html(kp, template)

            # Generate output filename
            output_file = output_path / f"{kp.class_name}.html"
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(html)

            print(f"Generated: {output_file}")
        except Exception as e:
            print(f"Error processing {java_file}: {e}")


if __name__ == '__main__':
    main()
