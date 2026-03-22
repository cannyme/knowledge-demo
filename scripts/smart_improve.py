#!/usr/bin/env python3
"""
智能内容感知型 HTML 改进脚本
- 将 ASCII 树形图转为 Mermaid
- 提取对比内容生成表格
- 结构化代码展示
"""

import re
from pathlib import Path
from typing import List, Dict, Tuple, Optional

class SmartContentProcessor:
    """智能内容处理器"""

    def __init__(self):
        self.ascii_tree_pattern = re.compile(r'^[│├└─|\s]+(.+)$', re.MULTILINE)
        self.code_block_pattern = re.compile(r'```(\w+)?\n(.*?)```', re.DOTALL)

    def detect_ascii_tree(self, content: str) -> Optional[str]:
        """检测并提取 ASCII 树形结构"""
        lines = content.split('\n')
        tree_lines = []
        in_tree = False

        for line in lines:
            if re.match(r'^[│├└─|\\\s]+\w+', line):
                tree_lines.append(line)
                in_tree = True
            elif in_tree and line.strip() == '':
                break
            elif in_tree:
                break

        if len(tree_lines) >= 3:
            return '\n'.join(tree_lines)
        return None

    def ascii_tree_to_mermaid(self, ascii_tree: str) -> str:
        """将 ASCII 树形图转换为 Mermaid"""
        lines = ascii_tree.strip().split('\n')

        # 判断是类图还是思维导图
        if any(':' in line for line in lines):
            return self._to_mermaid_mindmap(lines)
        else:
            return self._to_mermaid_tree(lines)

    def _to_mermaid_mindmap(self, lines: List[str]) -> str:
        """转换为 Mermaid 思维导图"""
        result = ['mindmap']
        stack = [0]  # 缩进层级栈

        for line in lines:
            # 计算层级
            indent = len(line) - len(line.lstrip(' │├└─|\\'))
            content = re.sub(r'^[│├└─|\\\s]+', '', line).strip()

            if not content:
                continue

            # 提取标题和描述
            if ':' in content:
                title, desc = content.split(':', 1)
                title = title.strip()
                desc = desc.strip()
                node_text = f'{title}<br/><small>{desc}</small>'
            else:
                node_text = content

            # 根据层级确定缩进
            level = min(indent // 2, 3)
            indent_str = '  ' * (level + 1)
            result.append(f'{indent_str}{node_text}')

        return '\n'.join(result)

    def _to_mermaid_tree(self, lines: List[str]) -> str:
        """转换为 Mermaid 树形图"""
        result = ['graph TD']
        node_map = {}
        node_counter = 0
        last_nodes = {0: 'Root'}

        for line in lines:
            content = re.sub(r'^[│├└─|\\\s]+', '', line).strip()
            if not content:
                continue

            # 计算层级
            indent = len(line) - len(line.lstrip(' │├└─|\\'))
            level = min(indent // 2 + 1, 4)

            # 创建节点
            node_id = f'N{node_counter}'
            node_counter += 1

            # 简化节点文本
            display_text = content.split(':')[0].strip() if ':' in content else content
            display_text = display_text[:30]  # 限制长度

            result.append(f'    {node_id}["{display_text}"]')

            # 连接父节点
            if level > 0 and (level - 1) in last_nodes:
                parent = last_nodes[level - 1]
                result.append(f'    {parent} --> {node_id}')

            last_nodes[level] = node_id

        return '\n'.join(result)

    def detect_comparison(self, sections: List[str]) -> Optional[Dict]:
        """检测是否有对比内容"""
        comparison_keywords = ['对比', 'vs', '比较', '区别', '选择']

        for section in sections:
            title = section.get('title', '')
            if any(kw in title for kw in comparison_keywords):
                return self._extract_comparison_table(section)
        return None

    def _extract_comparison_table(self, section: Dict) -> Optional[Dict]:
        """从 section 提取对比表格"""
        content = section.get('content', '')

        # 尝试提取对比项
        items = re.findall(r'([\w\s]+)[：:](.+?)(?=\n[\w\s]+[：:]|\Z)', content, re.DOTALL)

        if len(items) >= 2:
            return {
                'headers': ['特性', '说明'],
                'rows': [[item[0].strip(), item[1].strip()[:100]] for item in items[:6]]
            }
        return None

    def extract_code_examples(self, content: str) -> List[Dict]:
        """提取代码示例"""
        examples = []

        # 查找带注释的代码块
        pattern = r'(/\*\*.*?\*/)(\n.*?)(?=\n\s*/\*\*|$)'
        matches = list(re.finditer(pattern, content, re.DOTALL))

        for i, match in enumerate(matches[:3]):  # 最多3个示例
            comment = match.group(1)
            code = match.group(2)

            # 提取标题
            title_match = re.search(r'\*\s*(\w+)', comment)
            title = title_match.group(1) if title_match else f'示例 {i+1}'

            examples.append({
                'title': title,
                'description': comment,
                'code': code[:500]  # 限制长度
            })

        return examples

    def structure_content(self, content: str) -> List[Dict]:
        """将内容结构化"""
        blocks = []

        # 分割段落
        paragraphs = re.split(r'\n\s*\n', content)

        for para in paragraphs:
            para = para.strip()
            if not para:
                continue

            # 检测类型
            if para.startswith('/**') or para.startswith('/*'):
                # Java 注释
                blocks.append({'type': 'comment', 'content': para})
            elif 'public ' in para or 'private ' in para or 'class ' in para:
                # 代码块
                blocks.append({'type': 'code', 'content': para})
            elif re.match(r'^\d+\.', para) or para.startswith('•'):
                # 列表
                items = re.findall(r'(?:^|\n)\s*(?:\d+\.|•)\s*(.+)', para)
                blocks.append({'type': 'list', 'items': items})
            elif len(para) < 200:
                # 短段落
                blocks.append({'type': 'text', 'content': para})
            else:
                # 长段落
                blocks.append({'type': 'paragraph', 'content': para[:300] + '...'})

        return blocks


class KnowledgePageRewriter:
    """知识页面重写器"""

    def __init__(self):
        self.processor = SmartContentProcessor()

    def parse_html(self, filepath: Path) -> Dict:
        """解析现有 HTML"""
        content = filepath.read_text(encoding='utf-8')

        # 提取标题
        title_match = re.search(r'<title>(.+?)</title>', content)
        title = title_match.group(1).replace(' - Java Knowledge Review', '') if title_match else filepath.stem

        # 提取描述
        desc_match = re.search(r'<p class="kp-desc">(.+?)</p>', content, re.DOTALL)
        description = desc_match.group(1) if desc_match else ''

        # 提取所有 section 内容（原始格式）
        raw_sections = []
        section_pattern = r'<section[^>]*>.*?<div class="section-content">(.+?)</div>.*?</section>'
        for match in re.finditer(section_pattern, content, re.DOTALL):
            raw_content = match.group(1)
            # 清理 HTML 标签
            clean_content = re.sub(r'<p>', '\n', raw_content)
            clean_content = re.sub(r'</p>', '', clean_content)
            clean_content = re.sub(r'<li>', '\n• ', clean_content)
            clean_content = re.sub(r'</li>', '', clean_content)
            clean_content = re.sub(r'<h[1-6][^>]*>.*?<\/h[1-6]>', '', clean_content)
            clean_content = re.sub(r'<pre><code[^>]*>(.+?)</code></pre>', r'```\n\1\n```', clean_content, flags=re.DOTALL)
            clean_content = re.sub(r'<[^>]+>', '', clean_content)
            clean_content = re.sub(r'&lt;', '<', clean_content)
            clean_content = re.sub(r'&gt;', '>', clean_content)
            clean_content = re.sub(r'&quot;', '"', clean_content)
            clean_content = re.sub(r'&amp;', '&', clean_content)
            clean_content = re.sub(r'\n\s*\n', '\n', clean_content).strip()

            raw_sections.append(clean_content)

        # 合并所有内容
        full_content = '\n\n'.join(raw_sections)

        return {
            'title': title,
            'description': description,
            'content': full_content,
            'filename': filepath.stem
        }

    def generate_enhanced_html(self, data: Dict) -> str:
        """生成增强版 HTML"""
        filename = data['filename']
        content = data['content']

        # 检测 ASCII 树形图
        ascii_tree = self.processor.detect_ascii_tree(content)
        mermaid_diagrams = []

        if ascii_tree:
            mermaid_tree = self.processor.ascii_tree_to_mermaid(ascii_tree)
            mermaid_diagrams.append(('架构图', mermaid_tree))
        else:
            # 生成默认图表
            mermaid_diagrams.append(('架构图', self._get_default_diagram(filename)))

        # 提取代码示例
        code_examples = self.processor.extract_code_examples(content)

        # 结构化内容
        structured_blocks = self.processor.structure_content(content)

        # 生成 HTML
        return self._build_html(data, mermaid_diagrams, code_examples, structured_blocks)

    def _get_default_diagram(self, filename: str) -> str:
        """获取默认图表"""
        diagrams = {
            'CollectionFramework': '''classDiagram
    class Collection
    class List
    class Set
    class Queue
    class Map
    Collection <|-- List
    Collection <|-- Set
    Collection <|-- Queue
    List <|-- ArrayList
    List <|-- LinkedList
    Set <|-- HashSet
    Set <|-- TreeSet
    Map <|-- HashMap
    Map <|-- TreeMap''',
            'HashMapSource': '''flowchart TD
    A[put key, value] --> B{key == null?}
    B -->|是| C[放入下标0]
    B -->|否| D[计算hash]
    D --> E{存在冲突?}
    E -->|否| F[直接存入]
    E -->|是| G{是红黑树?}
    G -->|是| H[树节点插入]
    G -->|否| I[链表尾插]
    I --> J{链表长度>8?}
    J -->|是| K[转为红黑树]''',
        }
        return diagrams.get(filename, 'graph TD\n    A[开始] --> B[处理]\n    B --> C[结束]')

    def _build_html(self, data: Dict, diagrams: List[Tuple], code_examples: List[Dict], blocks: List[Dict]) -> str:
        """构建完整 HTML"""
        filename = data['filename']
        title = data['title']

        # 生成 Mermaid 图表 HTML
        diagrams_html = ''
        for name, diagram in diagrams:
            diagrams_html += f'''
      <div class="diagram-wrapper">
        <div class="diagram-title">📊 {name}</div>
        <div class="mermaid">{diagram}</div>
      </div>'''

        # 生成结构化内容 HTML
        content_html = ''
        for i, block in enumerate(blocks[:6]):  # 最多6个块
            if block['type'] == 'list':
                items_html = ''.join([f'<li>{item}</li>' for item in block['items'][:5]])
                content_html += f'''
        <div class="content-block">
          <ul class="feature-list">{items_html}</ul>
        </div>'''
            elif block['type'] == 'code':
                code = block['content'][:400]
                content_html += f'''
        <div class="code-preview">
          <div class="code-preview-header">
            <span class="code-lang">Java</span>
          </div>
          <pre><code>{code}</code></pre>
        </div>'''
            elif block['type'] == 'text':
                content_html += f'<p class="lead-text">{block["content"][:200]}</p>'

        # 生成代码示例 Tabs
        code_tabs_html = ''
        if code_examples:
            tabs_html = '<div class="tabs">\n'
            contents_html = ''

            for i, ex in enumerate(code_examples[:3]):
                tab_id = f"code-{i}"
                is_active = 'active' if i == 0 else ''
                tabs_html += f'            <button class="tab {is_active}" data-tab="{tab_id}">{ex["title"]}</button>\n'

                code = ex['code'].replace('<', '&lt;').replace('>', '&gt;')
                contents_html += f'''          <div class="tab-content {is_active}" id="{tab_id}">
            <div class="code-explanation">
              <p>{ex.get("description", "")[:150]}...</p>
            </div>
            <div class="code-container">
              <div class="code-header">
                <span class="code-lang">Java</span>
                <button class="code-btn" onclick="copyCode(this)">📋 复制</button>
              </div>
              <div class="code-body">
                <pre><code>{code}</code></pre>
              </div>
            </div>
          </div>
'''

            tabs_html += '          </div>\n'
            code_tabs_html = tabs_html + contents_html

        # 构建完整页面
        html = f'''<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{title} - Java Knowledge Review</title>
<script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
<style>
  @import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600;700&family=Outfit:wght@300;400;600;700;800;900&display=swap');

  :root {{
    --bg-deep: #0a0e17;
    --bg-card: rgba(16, 22, 36, 0.7);
    --bg-code: rgba(10, 14, 23, 0.9);
    --border-card: rgba(240, 180, 41, 0.12);
    --border-card-hover: rgba(240, 180, 41, 0.4);
    --amber: #f0b429;
    --amber-light: #ffd166;
    --amber-dim: rgba(240, 180, 41, 0.08);
    --cyan: #4ecdc4;
    --cyan-dim: rgba(78, 205, 196, 0.15);
    --green: #7ee787;
    --red: #ff7b72;
    --text-primary: #e8e6e3;
    --text-secondary: #8a8f98;
    --text-dim: #4a5068;
    --glow-amber: 0 0 30px rgba(240, 180, 41, 0.15);
    --font-code: 'JetBrains Mono', monospace;
    --font-heading: 'Outfit', sans-serif;
  }}

  [data-theme="light"] {{
    --bg-deep: #f8f6f1;
    --bg-card: rgba(255, 255, 255, 0.85);
    --bg-code: rgba(245, 243, 238, 0.95);
    --border-card: rgba(160, 120, 20, 0.18);
    --amber: #b8860b;
    --text-primary: #1a1a2e;
    --text-secondary: #4a4a5e;
  }}

  *, *::before, *::after {{ margin: 0; padding: 0; box-sizing: border-box; }}
  html {{ scroll-behavior: smooth; }}

  body {{
    background-color: var(--bg-deep);
    color: var(--text-primary);
    font-family: var(--font-heading);
    min-height: 100vh;
    line-height: 1.7;
  }}

  body::before {{
    content: '';
    position: fixed;
    inset: 0;
    background-image:
      linear-gradient(rgba(240, 180, 41, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(240, 180, 41, 0.03) 1px, transparent 1px);
    background-size: 60px 60px;
    pointer-events: none;
    z-index: 0;
  }}

  .container {{
    position: relative;
    z-index: 1;
    max-width: 1100px;
    margin: 0 auto;
    padding: 40px 24px 80px;
  }}

  .theme-toggle {{
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 999;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    border: 1px solid var(--border-card);
    background: var(--bg-card);
    backdrop-filter: blur(12px);
    cursor: pointer;
    font-size: 20px;
    transition: all 0.3s ease;
    box-shadow: var(--glow-amber);
  }}
  .theme-toggle:hover {{ border-color: var(--amber); transform: scale(1.1); }}

  .breadcrumb {{
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 24px;
    font-family: var(--font-code);
    font-size: 13px;
    color: var(--text-secondary);
  }}
  .breadcrumb a {{ color: var(--cyan); text-decoration: none; }}
  .breadcrumb a:hover {{ color: var(--amber); }}

  .hero {{
    background: var(--bg-card);
    border: 1px solid var(--border-card);
    border-radius: 20px;
    padding: 48px;
    margin-bottom: 32px;
    backdrop-filter: blur(12px);
    position: relative;
    overflow: hidden;
  }}
  .hero::before {{
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 4px;
    background: linear-gradient(90deg, var(--amber), var(--cyan), var(--amber));
  }}
  .hero-badge {{
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-family: var(--font-code);
    font-size: 12px;
    font-weight: 600;
    color: var(--cyan);
    background: var(--cyan-dim);
    border: 1px solid rgba(78, 205, 196, 0.2);
    border-radius: 20px;
    padding: 6px 16px;
    margin-bottom: 20px;
  }}
  .hero-title {{
    font-weight: 800;
    font-size: clamp(32px, 5vw, 48px);
    line-height: 1.1;
    margin-bottom: 16px;
    background: linear-gradient(135deg, var(--amber-light) 0%, var(--amber) 50%, var(--cyan) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }}
  .hero-desc {{
    font-size: 18px;
    color: var(--text-secondary);
    line-height: 1.8;
    max-width: 700px;
  }}

  .section {{
    background: var(--bg-card);
    border: 1px solid var(--border-card);
    border-radius: 16px;
    margin-bottom: 24px;
    overflow: hidden;
  }}
  .section-header {{
    padding: 24px 28px;
    border-bottom: 1px solid var(--border-card);
    display: flex;
    align-items: center;
    gap: 12px;
  }}
  .section-icon {{
    width: 36px;
    height: 36px;
    border-radius: 10px;
    background: linear-gradient(135deg, var(--amber-dim), var(--cyan-dim));
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
  }}
  .section-title {{
    font-weight: 700;
    font-size: 20px;
    color: var(--text-primary);
  }}
  .section-body {{
    padding: 28px;
  }}

  .diagram-wrapper {{
    background: var(--bg-code);
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 24px;
  }}
  .diagram-title {{
    font-size: 14px;
    font-weight: 600;
    color: var(--amber);
    margin-bottom: 16px;
    text-align: center;
  }}
  .mermaid {{
    display: flex;
    justify-content: center;
  }}

  .lead-text {{
    font-size: 16px;
    color: var(--text-secondary);
    line-height: 1.8;
    margin-bottom: 20px;
  }}

  .feature-list {{
    list-style: none;
    display: grid;
    gap: 12px;
  }}
  .feature-list li {{
    padding: 16px 20px;
    background: var(--bg-code);
    border-radius: 10px;
    border-left: 3px solid var(--amber);
    color: var(--text-secondary);
    font-size: 14px;
  }}
  .feature-list li::before {{
    content: '▸';
    color: var(--amber);
    margin-right: 8px;
  }}

  .code-preview {{
    background: var(--bg-code);
    border: 1px solid var(--border-card);
    border-radius: 12px;
    overflow: hidden;
    margin: 20px 0;
  }}
  .code-preview-header {{
    padding: 12px 16px;
    background: rgba(240, 180, 41, 0.05);
    border-bottom: 1px solid var(--border-card);
  }}
  .code-preview pre {{
    padding: 20px;
    overflow-x: auto;
    font-family: var(--font-code);
    font-size: 13px;
    line-height: 1.8;
    margin: 0;
    color: var(--text-secondary);
  }}

  .tabs {{
    display: flex;
    gap: 8px;
    padding: 20px 28px 0;
    border-bottom: 1px solid var(--border-card);
    overflow-x: auto;
  }}
  .tabs::-webkit-scrollbar {{ display: none; }}
  .tab {{
    padding: 12px 20px;
    background: transparent;
    border: none;
    border-bottom: 2px solid transparent;
    color: var(--text-secondary);
    font-family: var(--font-heading);
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
  }}
  .tab:hover {{ color: var(--text-primary); }}
  .tab.active {{
    color: var(--amber);
    border-bottom-color: var(--amber);
  }}
  .tab-content {{ display: none; }}
  .tab-content.active {{ display: block; }}

  .code-explanation {{
    padding: 20px 28px;
    background: var(--bg-code);
    border-bottom: 1px solid var(--border-card);
    font-size: 14px;
    color: var(--text-secondary);
  }}

  .code-container {{
    background: var(--bg-code);
    overflow: hidden;
  }}
  .code-header {{
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: rgba(240, 180, 41, 0.05);
    border-bottom: 1px solid var(--border-card);
  }}
  .code-lang {{
    font-family: var(--font-code);
    font-size: 11px;
    color: var(--amber);
    font-weight: 600;
  }}
  .code-btn {{
    font-family: var(--font-code);
    font-size: 11px;
    padding: 6px 12px;
    background: transparent;
    border: 1px solid var(--border-card);
    border-radius: 6px;
    color: var(--text-secondary);
    cursor: pointer;
    transition: all 0.2s;
  }}
  .code-btn:hover {{
    border-color: var(--amber);
    color: var(--amber);
  }}
  .code-body pre {{
    padding: 20px;
    overflow-x: auto;
    font-family: var(--font-code);
    font-size: 13px;
    line-height: 1.8;
    margin: 0;
    color: var(--text-primary);
  }}

  .footer {{
    text-align: center;
    margin-top: 60px;
    padding-top: 32px;
    border-top: 1px solid var(--border-card);
    font-family: var(--font-code);
    font-size: 12px;
    color: var(--text-dim);
  }}

  @media (max-width: 768px) {{
    .container {{ padding: 20px 16px 60px; }}
    .hero {{ padding: 28px; }}
    .section-body {{ padding: 20px; }}
  }}
</style>
</head>
<body>
<button class="theme-toggle" id="themeToggle">🌙</button>

<div class="container">
  <nav class="breadcrumb">
    <a href="../knowledge-map.html">📚 知识地图</a>
    <span>/</span>
    <span>{filename}</span>
  </nav>

  <header class="hero">
    <div class="hero-badge">🏷️ {filename}</div>
    <h1 class="hero-title">{title}</h1>
    <p class="hero-desc">{data.get('description', title + '的详细解析和最佳实践')[:150]}</p>
  </header>

  <section class="section">
    <div class="section-header">
      <div class="section-icon">🏗️</div>
      <h2 class="section-title">架构与原理</h2>
    </div>
    <div class="section-body">
      {diagrams_html}
      {content_html}
    </div>
  </section>

  <section class="section">
    <div class="section-header">
      <div class="section-icon">💻</div>
      <h2 class="section-title">代码实现</h2>
    </div>
    {code_tabs_html if code_tabs_html else '<div class="section-body"><p class="lead-text">暂无代码示例</p></div>'}
  </section>

  <footer class="footer">
    Java Knowledge Review Demo - 50 Knowledge Points<br>
    <a href="../knowledge-map.html" style="color: var(--cyan);">← 返回知识地图</a>
  </footer>
</div>

<script>
mermaid.initialize({{
  startOnLoad: true,
  theme: 'dark',
  themeVariables: {{
    primaryColor: '#f0b429',
    primaryTextColor: '#e8e6e3',
    lineColor: '#4ecdc4',
    background: '#0a0e17',
    mainBkg: '#0a0e17',
    secondBkg: '#101624',
    tertiaryColor: '#f0b429'
  }}
}});

document.getElementById('themeToggle').addEventListener('click', () => {{
  const html = document.documentElement;
  if (html.getAttribute('data-theme') === 'light') {{
    html.removeAttribute('data-theme');
    document.getElementById('themeToggle').textContent = '🌙';
    mermaid.initialize({{ theme: 'dark' }});
  }} else {{
    html.setAttribute('data-theme', 'light');
    document.getElementById('themeToggle').textContent = '☀️';
    mermaid.initialize({{ theme: 'default' }});
  }}
  mermaid.init();
}});

document.querySelectorAll('.tab').forEach(tab => {{
  tab.addEventListener('click', () => {{
    const parent = tab.closest('.section-body') || tab.closest('.section');
    parent.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    parent.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    tab.classList.add('active');
    const tabId = tab.getAttribute('data-tab');
    const content = parent.querySelector('#' + tabId);
    if (content) content.classList.add('active');
  }});
}});

function copyCode(btn) {{
  const code = btn.closest('.code-container, .code-preview').querySelector('code').textContent;
  navigator.clipboard.writeText(code).then(() => {{
    btn.textContent = '✓ 已复制';
    btn.style.color = '#4ecdc4';
    setTimeout(() => {{
      btn.textContent = '📋 复制';
      btn.style.color = '';
    }}, 2000);
  }});
}}
</script>
</body>
</html>'''
        return html

    def process_file(self, filepath: Path) -> bool:
        """处理单个文件"""
        try:
            print(f"  改进: {filepath.name}")
            data = self.parse_html(filepath)
            new_html = self.generate_enhanced_html(data)
            filepath.write_text(new_html, encoding='utf-8')
            return True
        except Exception as e:
            print(f"  ✗ 错误: {filepath.name} - {e}")
            return False

    def run(self, knowledge_dir: Path):
        """运行批量改进"""
        html_files = sorted(knowledge_dir.glob('*.html'))
        total = len(html_files)
        success = 0

        print(f"\n{'='*60}")
        print(f"智能内容感知型改进")
        print(f"{'='*60}")
        print(f"共发现 {total} 个文件\n")

        for i, filepath in enumerate(html_files, 1):
            print(f"[{i}/{total}] ", end="")
            if self.process_file(filepath):
                success += 1

        print(f"\n{'='*60}")
        print(f"完成: {success}/{total} 个文件改进成功")
        print(f"{'='*60}\n")

        return success, total


def main():
    script_dir = Path(__file__).parent.resolve()
    knowledge_dir = script_dir.parent / 'docs' / 'knowledge'

    if not knowledge_dir.exists():
        print(f"错误: 未找到知识库目录 {knowledge_dir}")
        return 1

    rewriter = KnowledgePageRewriter()
    success, total = rewriter.run(knowledge_dir)

    return 0 if success == total else 1


if __name__ == '__main__':
    exit(main())
