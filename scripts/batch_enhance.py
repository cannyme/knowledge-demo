#!/usr/bin/env python3
"""
批量增强知识点页面
- 使用预定义的模板和图表
- 保持内容结构清晰
"""

import re
from pathlib import Path
from typing import Dict, List, Tuple

class PageEnhancer:
    """页面增强器"""

    # 预定义的 Mermaid 图表
    DIAGRAMS = {
        'CollectionFramework': '''classDiagram
    class Iterable { <<interface>> }
    class Collection { <<interface>> }
    class List { <<interface>> }
    class Set { <<interface>> }
    class Queue { <<interface>> }
    class Map { <<interface>> }
    Iterable <|-- Collection
    Collection <|-- List
    Collection <|-- Set
    Collection <|-- Queue
    List <|.. ArrayList
    List <|.. LinkedList
    Set <|.. HashSet
    Set <|.. TreeSet
    Map <|.. HashMap
    Map <|.. TreeMap''',

        'HashMapSource': '''flowchart TD
    A[put key, value] --> B{key == null?}
    B -->|是| C[下标0]
    B -->|否| D[计算hash]
    D --> E{存在冲突?}
    E -->|否| F[直接存入]
    E -->|是| G{是红黑树?}
    G -->|是| H[树节点插入]
    G -->|否| I[链表尾插]
    I --> J{链表长度>8?}
    J -->|是| K[转为红黑树]''',

        'SingletonPattern': '''classDiagram
    class Singleton {
        -static Singleton instance
        -Singleton()
        +static Singleton getInstance()
    }''',

        'RedisDataStructures': '''flowchart LR
    A[客户端] -->|命令| B[Redis服务器]
    B --> C[内存存储]
    B --> D[String]
    B --> E[Hash]
    B --> F[List]
    B --> G[Set]
    B --> H[ZSet]''',

        'SpringAOP': '''flowchart TD
    A[目标方法] --> B[前置通知@Before]
    B --> C[目标方法执行]
    C --> D[后置通知@After]
    D --> E[返回通知@AfterReturning]
    C -.->|异常| F[异常通知@AfterThrowing]
    F --> G[方法结束]''',

        'ThreadPools': '''flowchart TD
    A[任务提交] --> B{核心线程满?}
    B -->|否| C[创建线程]
    B -->|是| D{队列满?}
    D -->|否| E[加入队列]
    D -->|是| F{最大线程满?}
    F -->|否| G[创建非核心]
    F -->|是| H[拒绝策略]''',

        'CircuitBreaker': '''stateDiagram-v2
    [*] --> CLOSED
    CLOSED --> OPEN: 失败率>阈值
    OPEN --> HALF_OPEN: 超时
    HALF_OPEN --> CLOSED: 成功
    HALF_OPEN --> OPEN: 失败''',

        'FactoryPattern': '''classDiagram
    class Product { <<interface>> }
    class ConcreteProductA
    class ConcreteProductB
    class Factory { +createProduct() }
    Product <|.. ConcreteProductA
    Product <|.. ConcreteProductB
    Factory ..> Product''',

        'ProxyPattern': '''classDiagram
    class Subject { <<interface>> }
    class RealSubject
    class Proxy
    Subject <|.. RealSubject
    Subject <|.. Proxy
    Proxy --> RealSubject''',

        'ObserverPattern': '''classDiagram
    class Subject {
        -observers: List
        +attach(Observer)
        +notify()
    }
    class Observer { <<interface>> +update() }
    Subject "1" --> "*" Observer''',

        'MySQLIndex': '''flowchart TD
    A[SQL查询] --> B[查询优化器]
    B --> C{使用索引?}
    C -->|是| D[索引查找]
    C -->|否| E[全表扫描]
    D --> F[返回结果]
    E --> F''',

        'TransactionIsolation': '''flowchart LR
    subgraph 读未提交
    A[脏读]
    end
    subgraph 读已提交
    B[不可重复读]
    end
    subgraph 可重复读
    C[幻读]
    end
    subgraph 串行化
    D[无问题]
    end''',

        'JVMMemoryModel': '''flowchart TD
    subgraph 运行时数据区
    A[堆 Heap] --> B[年轻代]
    A --> C[老年代]
    D[元空间 Metaspace]
    E[虚拟机栈]
    F[本地方法栈]
    G[程序计数器]
    end''',

        'ClassLoading': '''flowchart TD
    A[加载 Loading] --> B[验证 Verification]
    B --> C[准备 Preparation]
    C --> D[解析 Resolution]
    D --> E[初始化 Initialization]''',

        'LoadBalancer': '''flowchart TD
    A[客户端请求] --> B[负载均衡器]
    B --> C[实例1]
    B --> D[实例2]
    B --> E[实例3]''',

        'RocketMQ': '''flowchart LR
    A[生产者] -->|发送| B[Broker]
    B --> C[消费者]
    B --> D[消息存储]
    B --> E[消费确认]''',

        'Kafka': '''flowchart TD
    A[Producer] -->|发送| B[Topic Partition]
    B --> C[Consumer Group]
    B --> D[Replication]
    B --> E[Offset管理]''',
    }

    def __init__(self):
        self.template = self._load_template()

    def _load_template(self) -> str:
        """加载基础模板"""
        return '''<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{title} - Java Knowledge Review</title>
<script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
<style>
  @import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600;700&family=Outfit:wght@300;400;600;700;800;900&display=swap');
  :root {{
    --bg-deep: #0a0e17; --bg-card: rgba(16, 22, 36, 0.7); --bg-code: rgba(10, 14, 23, 0.9);
    --border-card: rgba(240, 180, 41, 0.12); --amber: #f0b429; --cyan: #4ecdc4;
    --green: #7ee787; --red: #ff7b72; --text-primary: #e8e6e3; --text-secondary: #8a8f98;
    --font-code: 'JetBrains Mono', monospace; --font-heading: 'Outfit', sans-serif;
  }}
  [data-theme="light"] {{
    --bg-deep: #f8f6f1; --bg-card: rgba(255, 255, 255, 0.85); --bg-code: rgba(245, 243, 238, 0.95);
    --border-card: rgba(160, 120, 20, 0.18); --amber: #b8860b; --text-primary: #1a1a2e;
  }}
  *, *::before, *::after {{ margin: 0; padding: 0; box-sizing: border-box; }}
  body {{ background: var(--bg-deep); color: var(--text-primary); font-family: var(--font-heading); min-height: 100vh; }}
  body::before {{ content: ''; position: fixed; inset: 0; background-image: linear-gradient(rgba(240, 180, 41, 0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(240, 180, 41, 0.03) 1px, transparent 1px); background-size: 60px 60px; pointer-events: none; z-index: 0; }}
  .container {{ position: relative; z-index: 1; max-width: 1100px; margin: 0 auto; padding: 40px 24px 80px; }}
  .theme-toggle {{ position: fixed; top: 20px; right: 20px; z-index: 999; width: 44px; height: 44px; border-radius: 50%; border: 1px solid var(--border-card); background: var(--bg-card); backdrop-filter: blur(12px); cursor: pointer; font-size: 20px; transition: all 0.3s; }}
  .theme-toggle:hover {{ border-color: var(--amber); transform: scale(1.1); }}
  .breadcrumb {{ display: flex; gap: 8px; margin-bottom: 24px; font-family: var(--font-code); font-size: 13px; color: var(--text-secondary); }}
  .breadcrumb a {{ color: var(--cyan); text-decoration: none; }} .breadcrumb a:hover {{ color: var(--amber); }}
  .hero {{ background: var(--bg-card); border: 1px solid var(--border-card); border-radius: 20px; padding: 48px; margin-bottom: 32px; position: relative; overflow: hidden; }}
  .hero::before {{ content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px; background: linear-gradient(90deg, var(--amber), var(--cyan), var(--amber)); }}
  .hero-badge {{ display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-code); font-size: 12px; font-weight: 600; color: var(--cyan); background: rgba(78, 205, 196, 0.15); border: 1px solid rgba(78, 205, 196, 0.2); border-radius: 20px; padding: 6px 16px; margin-bottom: 20px; }}
  .hero-title {{ font-weight: 800; font-size: clamp(32px, 5vw, 48px); background: linear-gradient(135deg, var(--amber), var(--cyan)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 16px; }}
  .hero-desc {{ font-size: 18px; color: var(--text-secondary); line-height: 1.8; max-width: 700px; }}
  .section {{ background: var(--bg-card); border: 1px solid var(--border-card); border-radius: 16px; margin-bottom: 24px; overflow: hidden; }}
  .section-header {{ padding: 24px 28px; border-bottom: 1px solid var(--border-card); display: flex; align-items: center; gap: 12px; }}
  .section-icon {{ width: 36px; height: 36px; border-radius: 10px; background: linear-gradient(135deg, rgba(240, 180, 41, 0.08), rgba(78, 205, 196, 0.15)); display: flex; align-items: center; justify-content: center; font-size: 18px; }}
  .section-title {{ font-weight: 700; font-size: 20px; color: var(--text-primary); }}
  .section-body {{ padding: 28px; color: var(--text-secondary); line-height: 1.8; }}
  .section-body p {{ margin-bottom: 12px; }}
  .section-body ul {{ margin: 12px 0; padding-left: 24px; }}
  .section-body li {{ margin-bottom: 8px; }}
  .mermaid {{ display: flex; justify-content: center; padding: 20px; background: var(--bg-code); border-radius: 12px; margin: 20px 0; }}
  .code-container {{ background: var(--bg-code); border: 1px solid var(--border-card); border-radius: 12px; overflow: hidden; margin: 20px 0; }}
  .code-header {{ display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: rgba(240, 180, 41, 0.05); border-bottom: 1px solid var(--border-card); }}
  .code-lang {{ font-family: var(--font-code); font-size: 11px; color: var(--amber); font-weight: 600; }}
  .code-btn {{ font-family: var(--font-code); font-size: 11px; padding: 6px 12px; background: transparent; border: 1px solid var(--border-card); border-radius: 6px; color: var(--text-secondary); cursor: pointer; }}
  .code-btn:hover {{ border-color: var(--amber); color: var(--amber); }}
  .code-body pre {{ padding: 20px; overflow-x: auto; font-family: var(--font-code); font-size: 13px; line-height: 1.8; margin: 0; color: var(--text-primary); }}
  .tabs {{ display: flex; gap: 8px; padding: 20px 28px 0; border-bottom: 1px solid var(--border-card); overflow-x: auto; }}
  .tab {{ padding: 12px 20px; background: transparent; border: none; border-bottom: 2px solid transparent; color: var(--text-secondary); font-family: var(--font-heading); font-size: 14px; font-weight: 600; cursor: pointer; }}
  .tab.active {{ color: var(--amber); border-bottom-color: var(--amber); }}
  .tab-content {{ display: none; }} .tab-content.active {{ display: block; }}
  .footer {{ text-align: center; margin-top: 60px; padding-top: 32px; border-top: 1px solid var(--border-card); font-family: var(--font-code); font-size: 12px; color: var(--text-dim); }}
  @media (max-width: 768px) {{ .container {{ padding: 20px 16px; }} .hero {{ padding: 28px; }} }}
</style>
</head>
<body>
<button class="theme-toggle" id="themeToggle">🌙</button>
<div class="container">
  <nav class="breadcrumb"><a href="../knowledge-map.html">📚 知识地图</a> <span>/</span> <span>{category}</span></nav>
  <header class="hero">
    <div class="hero-badge">{badge}</div>
    <h1 class="hero-title">{title}</h1>
    <p class="hero-desc">{description}</p>
  </header>
  {content}
  <footer class="footer">Java Knowledge Review Demo - 50 Knowledge Points<br><a href="../knowledge-map.html" style="color: var(--cyan);">← 返回知识地图</a></footer>
</div>
<script>
mermaid.initialize({{ startOnLoad: true, theme: 'dark', themeVariables: {{ primaryColor: '#f0b429', lineColor: '#4ecdc4', background: '#0a0e17' }} }});
document.getElementById('themeToggle').addEventListener('click', () => {{ const html = document.documentElement; if (html.getAttribute('data-theme') === 'light') {{ html.removeAttribute('data-theme'); document.getElementById('themeToggle').textContent = '🌙'; }} else {{ html.setAttribute('data-theme', 'light'); document.getElementById('themeToggle').textContent = '☀️'; }} }});
document.querySelectorAll('.tab').forEach(tab => {{ tab.addEventListener('click', () => {{ const parent = tab.closest('.section'); parent.querySelectorAll('.tab').forEach(t => t.classList.remove('active')); parent.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active')); tab.classList.add('active'); document.getElementById(tab.getAttribute('data-tab')).classList.add('active'); }}); }});
function copyCode(btn) {{ const code = btn.closest('.code-container').querySelector('code').textContent; navigator.clipboard.writeText(code).then(() => {{ btn.textContent = '✓ 已复制'; btn.style.color = '#4ecdc4'; setTimeout(() => {{ btn.textContent = '📋 复制'; btn.style.color = ''; }}, 2000); }}); }}
</script>
</body>
</html>'''

    def extract_content(self, html: str) -> Dict:
        """从现有 HTML 提取内容"""
        # 提取标题
        title_match = re.search(r'<title>(.+?) - Java', html)
        title = title_match.group(1) if title_match else '知识点'

        # 提取所有 section
        sections = []
        section_pattern = r'<section[^>]*>.*?<h2[^>]*>(.+?)</h2>.*?<div class="section-content">(.+?)</div>.*?</section>'
        for match in re.finditer(section_pattern, html, re.DOTALL):
            title_text = re.sub(r'<[^>]+>', '', match.group(1))
            content = match.group(2)
            # 清理内容
            content = re.sub(r'<p>', '\n<p>', content)
            content = re.sub(r'</p>', '</p>\n', content)
            content = re.sub(r'<li>', '\n<li>• ', content)
            content = re.sub(r'<pre><code[^>]*>(.+?)</code></pre>', self._format_code_block, content, flags=re.DOTALL)
            content = re.sub(r'<[^>]+>', '', content)
            content = re.sub(r'&lt;', '<', content)
            content = re.sub(r'&gt;', '>', content)
            content = re.sub(r'&quot;', '"', content)
            content = re.sub(r'&amp;', '&', content)
            content = re.sub(r'\n{3,}', '\n\n', content).strip()

            sections.append({'title': title_text, 'content': content[:800]})

        # 提取代码块
        codes = []
        code_pattern = r'<div class="code-block">.*?<span class="code-title">(.+?)</span>.*?<pre><code[^>]*>(.+?)</code></pre>.*?</div>'
        for match in re.finditer(code_pattern, html, re.DOTALL):
            title = match.group(1)
            code = match.group(2)
            code = code.replace('&lt;', '<').replace('&gt;', '>').replace('&quot;', '"').replace('&amp;', '&')
            codes.append({'title': title, 'code': code[:600]})

        return {'title': title, 'sections': sections, 'codes': codes}

    def _format_code_block(self, match) -> str:
        """格式化代码块"""
        code = match.group(1)
        code = code.replace('&lt;', '<').replace('&gt;', '>').replace('&quot;', '"').replace('&amp;', '&')
        return f'\n<div class="code-container"><div class="code-header"><span class="code-lang">Java</span></div><div class="code-body"><pre><code>{code[:500]}</code></pre></div></div>\n'

    def generate_content_html(self, data: Dict, filename: str) -> str:
        """生成内容 HTML"""
        sections_html = []

        # 架构图 section
        diagram = self.DIAGRAMS.get(filename, 'graph TD\n    A[开始] --> B[结束]')
        sections_html.append(f'''<section class="section">
    <div class="section-header"><div class="section-icon">🏗️</div><h2 class="section-title">架构原理</h2></div>
    <div class="section-body">
      <div class="mermaid">{diagram}</div>
    </div>
  </section>''')

        # 内容 sections
        for i, section in enumerate(data['sections'][:3]):
            icon = ['🔑', '📝', '💡'][i % 3]
            content_html = self._format_content(section['content'])
            sections_html.append(f'''<section class="section">
    <div class="section-header"><div class="section-icon">{icon}</div><h2 class="section-title">{section['title']}</h2></div>
    <div class="section-body">{content_html}</div>
  </section>''')

        # 代码 section
        if data['codes']:
            if len(data['codes']) > 1:
                tabs_html = '<div class="tabs">' + ''.join([f'<button class="tab {"active" if i==0 else ""}" data-tab="tab-{i}">{c["title"][:8]}</button>' for i, c in enumerate(data['codes'][:3])]) + '</div>'
                contents_html = ''
                for i, code in enumerate(data['codes'][:3]):
                    contents_html += f'<div class="tab-content {"active" if i==0 else ""}" id="tab-{i}"><div class="section-body"><div class="code-container"><div class="code-header"><span class="code-lang">Java</span><button class="code-btn" onclick="copyCode(this)">📋 复制</button></div><div class="code-body"><pre><code>{code["code"]}</code></pre></div></div></div></div>'
                sections_html.append(f'''<section class="section">{tabs_html}{contents_html}</section>''')
            else:
                code = data['codes'][0]
                sections_html.append(f'''<section class="section">
    <div class="section-header"><div class="section-icon">💻</div><h2 class="section-title">代码示例</h2></div>
    <div class="section-body"><div class="code-container"><div class="code-header"><span class="code-lang">Java</span><button class="code-btn" onclick="copyCode(this)">📋 复制</button></div><div class="code-body"><pre><code>{code["code"]}</code></pre></div></div></div>
  </section>''')

        return '\n'.join(sections_html)

    def _format_content(self, content: str) -> str:
        """格式化内容为 HTML"""
        lines = content.split('\n')
        result = []
        in_list = False

        for line in lines:
            line = line.strip()
            if not line:
                continue
            if line.startswith('•'):
                if not in_list:
                    result.append('<ul>')
                    in_list = True
                result.append(f'<li>{line[1:].strip()}</li>')
            else:
                if in_list:
                    result.append('</ul>')
                    in_list = False
                result.append(f'<p>{line}</p>')

        if in_list:
            result.append('</ul>')

        return '\n'.join(result[:20])  # 限制行数

    def process_file(self, filepath: Path) -> bool:
        """处理单个文件"""
        try:
            html = filepath.read_text(encoding='utf-8')
            data = self.extract_content(html)

            # 生成新内容
            content_html = self.generate_content_html(data, filepath.stem)

            # 填充模板
            category = self._get_category(filepath.stem)
            badge = self._get_badge(filepath.stem)
            new_html = self.template.format(
                title=data['title'],
                category=category,
                badge=badge,
                description=data['title'] + '的详细解析和最佳实践',
                content=content_html
            )

            filepath.write_text(new_html, encoding='utf-8')
            print(f"  ✓ {filepath.name}")
            return True
        except Exception as e:
            print(f"  ✗ {filepath.name}: {e}")
            return False

    def _get_category(self, filename: str) -> str:
        """获取分类"""
        categories = {
            'Collection': 'Java基础', 'HashMap': 'Java基础', 'List': 'Java基础',
            'Singleton': '设计模式', 'Factory': '设计模式', 'Proxy': '设计模式',
            'Redis': '缓存', 'MySQL': '数据库', 'Spring': 'Spring生态',
        }
        for key, cat in categories.items():
            if key in filename:
                return cat
        return '后端技术'

    def _get_badge(self, filename: str) -> str:
        """获取徽章"""
        badges = {
            'Collection': '📦 COLLECTIONS', 'HashMap': '🗺️ MAP',
            'Singleton': '🏷️ SINGLETON', 'Factory': '🏭 FACTORY',
            'Redis': '⚡ CACHE', 'Spring': '🌱 SPRING',
        }
        for key, badge in badges.items():
            if key in filename:
                return badge
        return '📚 KNOWLEDGE'

    def run(self, knowledge_dir: Path):
        """批量处理"""
        files = sorted(knowledge_dir.glob('*.html'))
        print(f"\n批量增强 {len(files)} 个页面...\n")
        success = sum(1 for f in files if self.process_file(f))
        print(f"\n完成: {success}/{len(files)}\n")


def main():
    script_dir = Path(__file__).parent.resolve()
    knowledge_dir = script_dir.parent / 'docs' / 'knowledge'
    if not knowledge_dir.exists():
        print(f"错误: 目录不存在 {knowledge_dir}")
        return 1

    enhancer = PageEnhancer()
    enhancer.run(knowledge_dir)
    return 0


if __name__ == '__main__':
    exit(main())
