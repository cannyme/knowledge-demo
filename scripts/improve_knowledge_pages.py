#!/usr/bin/env python3
"""
Knowledge Pages UI Improvement Script
批量改进知识点 HTML 页面的视觉设计和交互体验
"""

import re
import os
import json
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Optional, Tuple

class KnowledgePageImprover:
    """知识点页面改进器"""

    # 知识点分类映射
    CATEGORY_MAP = {
        # 设计模式
        'SingletonPattern': ('designpatterns', '创建型模式', 'creational'),
        'FactoryPattern': ('designpatterns', '创建型模式', 'creational'),
        'BuilderPattern': ('designpatterns', '创建型模式', 'creational'),
        'ProxyPattern': ('designpatterns', '结构型模式', 'structural'),
        'DecoratorPattern': ('designpatterns', '结构型模式', 'structural'),
        'AdapterPattern': ('designpatterns', '结构型模式', 'structural'),
        'StrategyPattern': ('designpatterns', '行为型模式', 'behavioral'),
        'ObserverPattern': ('designpatterns', '行为型模式', 'behavioral'),
        'TemplateMethodPattern': ('designpatterns', '行为型模式', 'behavioral'),
        'ChainOfResponsibility': ('designpatterns', '行为型模式', 'behavioral'),

        # 数据库
        'MySQLIndex': ('mysql', '数据库', 'database'),
        'TransactionIsolation': ('mysql', '数据库', 'database'),
        'MVCC': ('mysql', '数据库', 'database'),
        'Sharding': ('database', '数据库架构', 'architecture'),
        'ConnectionPool': ('database', '性能优化', 'performance'),
        'RedisDataStructures': ('redis', '缓存', 'cache'),
        'RedisHighAvailability': ('redis', '高可用', 'ha'),
        'DistributedLock': ('distributed', '分布式系统', 'distributed'),
        'DistributedTransaction': ('distributed', '分布式系统', 'distributed'),

        # 消息队列
        'RocketMQ': ('mq', '消息队列', 'mq'),
        'Kafka': ('mq', '消息队列', 'mq'),
        'MessageReliability': ('mq', '消息队列', 'mq'),

        # Spring 生态
        'IoCAndDI': ('spring', 'Spring核心', 'core'),
        'SpringAOP': ('spring', 'Spring核心', 'core'),
        'TransactionManagement': ('spring', 'Spring核心', 'core'),
        'SpringSecurity': ('spring', 'Spring安全', 'security'),
        'AutoConfiguration': ('springboot', 'SpringBoot', 'boot'),
        'MyBatisUsage': ('mybatis', 'ORM框架', 'orm'),

        # Spring Cloud
        'ServiceDiscovery': ('springcloud', '微服务', 'microservice'),
        'LoadBalancer': ('springcloud', '微服务', 'microservice'),
        'CircuitBreaker': ('springcloud', '微服务', 'microservice'),
        'ApiGateway': ('springcloud', '微服务', 'microservice'),
        'FeignClient': ('springcloud', '微服务', 'microservice'),
        'NacosAndApollo': ('springcloud', '配置中心', 'config'),
        'SentinelFlowControl': ('springcloud', '限流熔断', 'flow'),
        'DubboRPC': ('springcloud', 'RPC框架', 'rpc'),

        # Java 基础
        'ClassLoading': ('jvm', 'JVM', 'jvm'),
        'JVMMemoryModel': ('jvm', 'JVM', 'jvm'),
        'ThreadPools': ('concurrency', '并发编程', 'concurrency'),
        'ThreadSafety': ('concurrency', '并发编程', 'concurrency'),
        'Java8Features': ('java8', 'Java特性', 'java8'),
        'CollectionFramework': ('collections', '集合框架', 'collections'),
        'HashMapSource': ('collections', '集合框架', 'collections'),

        # 系统架构
        'SystemDesign': ('architecture', '系统设计', 'system'),
        'DistributedTracing': ('architecture', '可观测性', 'observability'),
        'DockerKubernetes': ('architecture', '容器化', 'container'),

        # 网络
        'TCPAndHTTP': ('network', '网络协议', 'network'),
        'NettyNIO': ('network', '网络编程', 'network'),

        # 算法与数据结构
        'SortingAlgorithm': ('algorithm', '算法', 'algorithm'),
        'AdvancedDataStructures': ('datastructure', '数据结构', 'ds'),
    }

    # 图标映射
    ICONS = {
        'designpatterns': '🎨',
        'mysql': '🗄️',
        'redis': '⚡',
        'mq': '📨',
        'spring': '🌱',
        'springboot': '🚀',
        'springcloud': '☁️',
        'jvm': '☕',
        'concurrency': '🔀',
        'architecture': '🏗️',
        'network': '🌐',
        'algorithm': '📊',
        'database': '💾',
        'microservice': '🔧',
        'cache': '⚡',
    }

    def __init__(self, knowledge_dir: str):
        self.knowledge_dir = Path(knowledge_dir)
        self.improved_dir = self.knowledge_dir / 'improved'
        self.improved_dir.mkdir(exist_ok=True)

    def parse_html(self, filepath: Path) -> Dict:
        """解析现有 HTML 文件，提取关键内容"""
        content = filepath.read_text(encoding='utf-8')

        # 提取标题
        title_match = re.search(r'<title>(.+?)</title>', content)
        title = title_match.group(1).replace(' - Java Knowledge Review', '') if title_match else filepath.stem

        # 提取包名
        package_match = re.search(r'<div class="kp-tag">(.+?)</div>', content)
        package = package_match.group(1) if package_match else ''

        # 提取描述
        desc_match = re.search(r'<p class="kp-desc">(.+?)</p>', content, re.DOTALL)
        description = desc_match.group(1) if desc_match else ''

        # 提取所有 section
        sections = []
        section_pattern = r'<section class="kp-section[^"]*">\s*<h2 class="section-title">(.+?)</h2>\s*<div class="section-content">(.+?)</div>\s*</section>'
        for match in re.finditer(section_pattern, content, re.DOTALL):
            section_title = match.group(1)
            section_content = match.group(2)

            # 清理内容
            section_content = re.sub(r'<p>', '', section_content)
            section_content = re.sub(r'</p>', '\n', section_content)
            section_content = re.sub(r'<li>', '• ', section_content)
            section_content = re.sub(r'</li>', '\n', section_content)
            section_content = re.sub(r'<[^>]+>', '', section_content)
            section_content = re.sub(r'\n\s*\n', '\n', section_content).strip()

            sections.append({
                'title': section_title,
                'content': section_content
            })

        # 提取代码块
        code_blocks = []
        code_pattern = r'<div class="code-block">.*?<span class="code-title">(.+?)</span>.*?<pre><code[^>]*>(.+?)</code></pre>.*?</div>'
        for match in re.finditer(code_pattern, content, re.DOTALL):
            code_title = match.group(1)
            code_content = match.group(2)
            # 解码 HTML 实体
            code_content = code_content.replace('&lt;', '<').replace('&gt;', '>').replace('&quot;', '"').replace('&amp;', '&')
            code_blocks.append({
                'title': code_title,
                'code': code_content
            })

        return {
            'title': title,
            'package': package,
            'description': description,
            'sections': sections,
            'code_blocks': code_blocks,
            'filename': filepath.stem
        }

    def get_category_info(self, filename: str) -> Tuple[str, str, str]:
        """获取知识点的分类信息"""
        return self.CATEGORY_MAP.get(filename, ('general', '通用', 'general'))

    def generate_mermaid_diagram(self, filename: str) -> str:
        """根据知识点类型生成合适的 Mermaid 图表"""

        if 'Singleton' in filename:
            return '''classDiagram
    class Singleton {
        -static Singleton instance
        -Singleton()
        +static Singleton getInstance()
    }
    Client --> Singleton : getInstance()'''

        elif 'Factory' in filename:
            return '''classDiagram
    class Product {
        <<interface>>
        +use()
    }
    class ConcreteProductA
    class ConcreteProductB
    class Factory {
        +createProduct(type): Product
    }
    Product <|-- ConcreteProductA
    Product <|-- ConcreteProductB
    Factory ..> Product'''

        elif 'Proxy' in filename:
            return '''classDiagram
    class Subject {
        <<interface>>
        +request()
    }
    class RealSubject
    class Proxy
    Subject <|.. RealSubject
    Subject <|.. Proxy
    Proxy --> RealSubject'''

        elif 'Observer' in filename:
            return '''classDiagram
    class Subject {
        -observers: List~Observer~
        +attach(Observer)
        +detach(Observer)
        +notify()
    }
    class Observer {
        <<interface>>
        +update()
    }
    class ConcreteObserver
    Subject "1" --> "*" Observer
    Observer <|.. ConcreteObserver'''

        elif 'Redis' in filename:
            return '''flowchart LR
    A[客户端] -->|命令| B[Redis服务器]
    B --> C[内存存储]
    B --> D[RDB持久化]
    B --> E[AOF持久化]'''

        elif 'MySQL' in filename or 'Index' in filename:
            return '''flowchart TD
    A[SQL查询] --> B[查询优化器]
    B --> C{使用索引?}
    C -->|是| D[索引查找]
    C -->|否| E[全表扫描]
    D --> F[返回结果]
    E --> F'''

        elif 'Spring' in filename or 'IoC' in filename:
            return '''flowchart TD
    A[配置元数据] --> B[IoC容器]
    B --> C[Bean定义]
    C --> D[实例化]
    D --> E[依赖注入]
    E --> F[初始化]
    F --> G[就绪使用]'''

        elif 'Transaction' in filename:
            return '''flowchart TD
    A[开始事务] --> B[执行业务逻辑]
    B --> C{是否异常?}
    C -->|否| D[提交事务]
    C -->|是| E[回滚事务]
    D --> F[结束]
    E --> F'''

        elif 'CircuitBreaker' in filename:
            return '''stateDiagram-v2
    [*] --> CLOSED
    CLOSED --> OPEN: 失败率 > 阈值
    OPEN --> HALF_OPEN: 超时后
    HALF_OPEN --> CLOSED: 成功
    HALF_OPEN --> OPEN: 失败'''

        elif 'JVM' in filename or 'ClassLoading' in filename:
            return '''flowchart TD
    A[加载 Loading] --> B[验证 Verification]
    B --> C[准备 Preparation]
    C --> D[解析 Resolution]
    D --> E[初始化 Initialization]'''

        elif 'ThreadPool' in filename:
            return '''flowchart TD
    A[任务提交] --> B{核心线程满?}
    B -->|否| C[创建线程]
    B -->|是| D{队列满?}
    D -->|否| E[加入队列]
    D -->|是| F{最大线程满?}
    F -->|否| G[创建非核心线程]
    F -->|是| H[拒绝策略]'''

        elif 'Message' in filename or 'MQ' in filename or 'RocketMQ' in filename or 'Kafka' in filename:
            return '''flowchart LR
    A[生产者] -->|发送消息| B[消息队列]
    B -->|推送/拉取| C[消费者]
    B --> D[消息存储]
    B --> E[消费确认]'''

        elif 'LoadBalancer' in filename:
            return '''flowchart TD
    A[客户端请求] --> B[负载均衡器]
    B --> C[服务实例1]
    B --> D[服务实例2]
    B --> E[服务实例3]
    C --> F[返回响应]
    D --> F
    E --> F'''

        else:
            return '''flowchart TD
    A[开始] --> B[处理]
    B --> C[结束]'''

    def generate_key_points(self, filename: str, sections: List[Dict]) -> str:
        """生成关键要点卡片"""
        points = []

        # 从 sections 中提取关键信息
        for section in sections[:3]:  # 最多取前3个
            title = section['title']
            content = section['content'][:100] + '...' if len(section['content']) > 100 else section['content']

            if '优点' in title or '优势' in title:
                points.append({
                    'type': 'success',
                    'icon': '✓',
                    'title': title,
                    'content': content
                })
            elif '缺点' in title or '问题' in title or '注意' in title:
                points.append({
                    'type': 'warning',
                    'icon': '!',
                    'title': title,
                    'content': content
                })
            elif '原理' in title or '核心' in title:
                points.append({
                    'type': '',
                    'icon': '🔑',
                    'title': title,
                    'content': content
                })

        # 如果提取的点太少，添加默认要点
        if len(points) < 2:
            defaults = {
                'Singleton': [
                    {'type': 'success', 'icon': '⭐', 'title': '推荐实现', 'content': '静态内部类和枚举是最推荐的实现方式'},
                    {'type': 'warning', 'icon': '⚠️', 'title': '线程安全', 'content': '非同步懒汉式在多线程环境下不安全'},
                ],
                'Redis': [
                    {'type': 'success', 'icon': '⚡', 'title': '高性能', 'content': '基于内存操作，单线程模型避免上下文切换'},
                    {'type': '', 'icon': '📊', 'title': '丰富数据结构', 'content': '支持String、Hash、List、Set、ZSet等多种类型'},
                ],
                'Spring': [
                    {'type': 'success', 'icon': '🌱', 'title': '控制反转', 'content': '将对象创建和依赖关系交给容器管理'},
                    {'type': '', 'icon': '🔧', 'title': '依赖注入', 'content': '通过构造器、Setter或字段注入依赖'},
                ],
            }

            for key, default_points in defaults.items():
                if key in filename:
                    points.extend(default_points)
                    break

        # 生成 HTML
        html = '<div class="key-points">\n'
        for point in points[:3]:
            type_class = point.get('type', '')
            html += f'''            <div class="key-point {type_class}">
              <div class="key-point-icon">{point['icon']}</div>
              <div class="key-point-content">
                <h4>{point['title']}</h4>
                <p>{point['content']}</p>
              </div>
            </div>
'''
        html += '          </div>'
        return html

    def generate_code_tabs(self, code_blocks: List[Dict], filename: str) -> str:
        """生成代码 Tab 组件"""
        if len(code_blocks) <= 1:
            # 只有一个代码块，直接显示
            if not code_blocks:
                return ''
            block = code_blocks[0]
            return self._generate_code_block(block['title'], block['code'], filename)

        # 多个代码块，使用 Tab
        tabs_html = '<div class="tabs">\n'
        contents_html = ''

        for i, block in enumerate(code_blocks[:5]):  # 最多5个tab
            tab_id = f"tab-{i}"
            is_active = 'active' if i == 0 else ''

            # 提取简短的 tab 名称
            tab_name = block['title']
            if len(tab_name) > 8:
                tab_name = tab_name[:8] + '...'

            tabs_html += f'          <button class="tab {is_active}" data-tab="{tab_id}">{tab_name}</button>\n'

            code_html = self._generate_code_block_html(block['title'], block['code'])
            contents_html += f'''          <div class="tab-content {is_active}" id="{tab_id}">
            <div class="section-body">
              {code_html}
            </div>
          </div>
'''

        tabs_html += '        </div>\n'

        return tabs_html + contents_html

    def _generate_code_block(self, title: str, code: str, filename: str) -> str:
        """生成单个代码块"""
        return f'''        <div class="section-body">
          {self._generate_code_block_html(title, code)}
        </div>'''

    def _generate_code_block_html(self, title: str, code: str) -> str:
        """生成代码块 HTML"""
        # 语法高亮（简单版本）
        code_escaped = code.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')

        # 添加语法高亮类
        code_highlighted = code_escaped
        keywords = ['public', 'private', 'protected', 'static', 'final', 'class', 'interface',
                   'extends', 'implements', 'void', 'return', 'if', 'else', 'for', 'while',
                   'try', 'catch', 'new', 'this', 'super', 'import', 'package', 'enum']
        for kw in keywords:
            code_highlighted = re.sub(rf'\b{kw}\b', f'<span class="keyword">{kw}</span>', code_highlighted)

        # 字符串高亮
        code_highlighted = re.sub(r'(".*?")', r'<span class="string">\1</span>', code_highlighted)

        # 注释高亮
        code_highlighted = re.sub(r'(//.*?$)', r'<span class="comment">\1</span>', code_highlighted, flags=re.MULTILINE)

        # 判断是否需要折叠（代码超过15行）
        line_count = code.count('\n') + 1
        collapsed_class = 'collapsed' if line_count > 15 else ''
        expand_btn = f'<button class="code-expand" onclick="toggleExpand(this)">展开全部 ↓</button>' if line_count > 15 else ''

        return f'''<div class="code-container">
            <div class="code-header">
              <div class="code-meta">
                <span class="code-lang">Java</span>
              </div>
              <div class="code-actions">
                <button class="code-btn" onclick="copyCode(this)">📋 复制</button>
              </div>
            </div>
            <div class="code-body {collapsed_class}">
              <pre><code>{code_highlighted}</code></pre>
              {expand_btn}
            </div>
          </div>'''

    def generate_html(self, data: Dict) -> str:
        """生成改进后的 HTML"""
        filename = data['filename']
        category, subcategory, cat_code = self.get_category_info(filename)
        icon = self.ICONS.get(category, '📚')

        # 提取简短的标题
        short_title = data['title'].split('(')[0].strip()
        if len(short_title) > 20:
            short_title = short_title[:20]

        # 生成 Mermaid 图表
        mermaid_diagram = self.generate_mermaid_diagram(filename)

        # 生成关键要点
        key_points = self.generate_key_points(filename, data['sections'])

        # 生成代码区域
        code_section = self.generate_code_tabs(data['code_blocks'], filename)

        # 构建内容 sections
        sections_html = ''
        for section in data['sections'][:4]:  # 最多4个section
            section_icon = '📝'
            if '核心' in section['title'] or '原理' in section['title']:
                section_icon = '🔑'
            elif '实现' in section['title']:
                section_icon = '⚙️'
            elif '应用' in section['title']:
                section_icon = '📱'

            # 格式化内容
            content_lines = section['content'].split('\n')
            formatted_content = ''
            for line in content_lines:
                line = line.strip()
                if line.startswith('•') or line.startswith('-'):
                    formatted_content += f'<li>{line[1:].strip()}</li>\n'
                elif line:
                    formatted_content += f'<p>{line}</p>\n'

            sections_html += f'''
        <section class="section">
          <div class="section-header">
            <div class="section-icon">{section_icon}</div>
            <h2 class="section-title">{section['title']}</h2>
          </div>
          <div class="section-body">
            {formatted_content}
          </div>
        </section>
'''

        # 构建完整 HTML
        html = f'''<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{short_title} - Java Knowledge Review</title>
<script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
<style>
  @import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600;700&family=Outfit:wght@300;400;600;700;800;900&display=swap');

  :root {{
    --bg-deep: #0a0e17;
    --bg-card: rgba(16, 22, 36, 0.7);
    --bg-code: rgba(10, 14, 23, 0.9);
    --bg-inline: rgba(240, 180, 41, 0.08);
    --border-card: rgba(240, 180, 41, 0.12);
    --border-card-hover: rgba(240, 180, 41, 0.4);
    --amber: #f0b429;
    --amber-light: #ffd166;
    --amber-dim: rgba(240, 180, 41, 0.08);
    --cyan: #4ecdc4;
    --cyan-dim: rgba(78, 205, 196, 0.15);
    --green: #7ee787;
    --green-dim: rgba(126, 231, 135, 0.15);
    --red: #ff7b72;
    --red-dim: rgba(255, 123, 114, 0.15);
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
    --amber-light: #996600;
    --green: #2e7d32;
    --red: #d32f2f;
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
    display: flex;
    align-items: center;
    justify-content: center;
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
  .breadcrumb .sep {{ color: var(--text-dim); }}

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
  .section-body p {{
    margin-bottom: 12px;
    color: var(--text-secondary);
  }}
  .section-body li {{
    margin-bottom: 8px;
    color: var(--text-secondary);
    list-style: none;
    padding-left: 20px;
    position: relative;
  }}
  .section-body li::before {{
    content: '▸';
    position: absolute;
    left: 0;
    color: var(--amber);
  }}

  .mermaid {{
    display: flex;
    justify-content: center;
    padding: 20px;
    background: var(--bg-code);
    border-radius: 12px;
    margin: 20px 0;
  }}

  .key-points {{
    display: grid;
    gap: 16px;
    margin: 24px 0;
  }}
  .key-point {{
    display: flex;
    gap: 16px;
    padding: 20px;
    background: var(--bg-code);
    border-radius: 12px;
    border-left: 3px solid var(--cyan);
  }}
  .key-point.warning {{ border-left-color: var(--amber); }}
  .key-point.danger {{ border-left-color: var(--red); }}
  .key-point.success {{ border-left-color: var(--green); }}
  .key-point-icon {{
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: var(--cyan-dim);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }}
  .key-point.warning .key-point-icon {{ background: var(--amber-dim); }}
  .key-point.danger .key-point-icon {{ background: var(--red-dim); }}
  .key-point.success .key-point-icon {{ background: var(--green-dim); }}
  .key-point-content h4 {{
    font-weight: 600;
    font-size: 15px;
    margin-bottom: 6px;
    color: var(--text-primary);
  }}
  .key-point-content p {{
    font-size: 14px;
    color: var(--text-secondary);
    line-height: 1.6;
    margin: 0;
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

  .code-container {{
    background: var(--bg-code);
    border: 1px solid var(--border-card);
    border-radius: 12px;
    overflow: hidden;
    margin: 20px 0;
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
  }}
  .code-body.collapsed pre {{
    max-height: 200px;
    overflow: hidden;
  }}
  .code-body.collapsed::after {{
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 80px;
    background: linear-gradient(transparent, var(--bg-code));
    pointer-events: none;
  }}
  .code-expand {{
    position: absolute;
    bottom: 16px;
    left: 50%;
    transform: translateX(-50%);
    padding: 8px 20px;
    background: var(--bg-card);
    border: 1px solid var(--border-card);
    border-radius: 20px;
    color: var(--text-secondary);
    font-size: 12px;
    cursor: pointer;
    transition: all 0.2s;
    z-index: 10;
    display: none;
  }}
  .code-body.collapsed .code-expand {{ display: block; }}
  .code-expand:hover {{ border-color: var(--amber); color: var(--amber); }}

  .keyword {{ color: #c792ea; }}
  .string {{ color: #c3e88d; }}
  .comment {{ color: #676e95; font-style: italic; }}
  .code-body {{ position: relative; }}

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
    .section-header {{ padding: 20px; }}
    .section-body {{ padding: 20px; }}
    .tabs {{ padding: 16px 20px 0; }}
  }}
</style>
</head>
<body>
<button class="theme-toggle" id="themeToggle">🌙</button>

<div class="container">
  <nav class="breadcrumb">
    <a href="../knowledge-map.html">📚 知识地图</a>
    <span class="sep">/</span>
    <span>{subcategory}</span>
  </nav>

  <header class="hero">
    <div class="hero-badge">
      <span>{icon}</span>
      <span>{cat_code.upper()}</span>
    </div>
    <h1 class="hero-title">{short_title}</h1>
    <p class="hero-desc">{data['description'][:200] if data['description'] else short_title + '的详细解析和最佳实践'}</p>
  </header>

  <section class="section">
    <div class="section-header">
      <div class="section-icon">🏗️</div>
      <h2 class="section-title">架构原理</h2>
    </div>
    <div class="section-body">
      <div class="mermaid">
{mermaid_diagram}
      </div>
      {key_points}
    </div>
  </section>

{sections_html}

  <section class="section">
    <div class="section-header">
      <div class="section-icon">💻</div>
      <h2 class="section-title">代码实现</h2>
    </div>
{code_section}
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
    background: '#0a0e17'
  }}
}});

document.getElementById('themeToggle').addEventListener('click', () => {{
  const html = document.documentElement;
  if (html.getAttribute('data-theme') === 'light') {{
    html.removeAttribute('data-theme');
    document.getElementById('themeToggle').textContent = '🌙';
  }} else {{
    html.setAttribute('data-theme', 'light');
    document.getElementById('themeToggle').textContent = '☀️';
  }}
}});

document.querySelectorAll('.tab').forEach(tab => {{
  tab.addEventListener('click', () => {{
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    tab.classList.add('active');
    document.getElementById(tab.getAttribute('data-tab')).classList.add('active');
  }});
}});

function copyCode(btn) {{
  const code = btn.closest('.code-container').querySelector('code').textContent;
  navigator.clipboard.writeText(code).then(() => {{
    btn.textContent = '✓ 已复制';
    btn.style.color = '#4ecdc4';
    setTimeout(() => {{
      btn.textContent = '📋 复制';
      btn.style.color = '';
    }}, 2000);
  }});
}}

function toggleExpand(btn) {{
  const codeBody = btn.closest('.code-body');
  codeBody.classList.toggle('collapsed');
  btn.textContent = codeBody.classList.contains('collapsed') ? '展开全部 ↓' : '收起 ↑';
}}
</script>
</body>
</html>'''

        return html

    def improve_file(self, filepath: Path) -> bool:
        """改进单个文件"""
        try:
            print(f"  处理: {filepath.name}")
            data = self.parse_html(filepath)
            new_html = self.generate_html(data)

            # 保存到原位置
            filepath.write_text(new_html, encoding='utf-8')
            return True
        except Exception as e:
            print(f"  ✗ 错误: {filepath.name} - {str(e)}")
            return False

    def run(self):
        """运行批量改进"""
        html_files = sorted(self.knowledge_dir.glob('*.html'))
        total = len(html_files)
        success = 0

        print(f"\n{'='*60}")
        print(f"批量改进知识点页面")
        print(f"{'='*60}")
        print(f"共发现 {total} 个文件\n")

        for i, filepath in enumerate(html_files, 1):
            print(f"[{i}/{total}] ", end="")
            if self.improve_file(filepath):
                success += 1

        print(f"\n{'='*60}")
        print(f"完成: {success}/{total} 个文件改进成功")
        print(f"{'='*60}\n")

        return success, total


def main():
    """主函数"""
    # 确定知识库目录
    script_dir = Path(__file__).parent.resolve()
    knowledge_dir = script_dir.parent / 'docs' / 'knowledge'

    if not knowledge_dir.exists():
        print(f"错误: 未找到知识库目录 {knowledge_dir}")
        return 1

    improver = KnowledgePageImprover(knowledge_dir)
    success, total = improver.run()

    return 0 if success == total else 1


if __name__ == '__main__':
    exit(main())
