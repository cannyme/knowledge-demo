/**
 * Java Knowledge Review - 导航模块
 * 提供上一页/下一页按钮和搜索框功能
 * 使用安全的 DOM 操作方法（不使用 innerHTML）
 */
(function() {
  'use strict';

  // 页面配置列表 - 定义所有知识页面的顺序
  var pages = [
    { id: 'SingletonPattern', title: '单例模式', category: '设计模式' },
    { id: 'FactoryPattern', title: '工厂模式', category: '设计模式' },
    { id: 'BuilderPattern', title: '建造者模式', category: '设计模式' },
    { id: 'ProxyPattern', title: '代理模式', category: '设计模式' },
    { id: 'DecoratorPattern', title: '装饰器模式', category: '设计模式' },
    { id: 'ObserverPattern', title: '观察者模式', category: '设计模式' },
    { id: 'StrategyPattern', title: '策略模式', category: '设计模式' },
    { id: 'TemplateMethodPattern', title: '模板方法模式', category: '设计模式' },
    { id: 'AdapterPattern', title: '适配器模式', category: '设计模式' },
    { id: 'ChainOfResponsibility', title: '责任链模式', category: '设计模式' },
    { id: 'ThreadSafety', title: '线程安全', category: '并发编程' },
    { id: 'ThreadPools', title: '线程池', category: '并发编程' },
    { id: 'CollectionFramework', title: '集合框架', category: '集合' },
    { id: 'JVMMemoryModel', title: 'JVM内存模型', category: 'JVM' },
    { id: 'ClassLoading', title: '类加载机制', category: 'JVM' },
    { id: 'Java8Features', title: 'Java 8特性', category: 'Java 8' },
    { id: 'IoCAndDI', title: 'IoC与DI', category: 'Spring' },
    { id: 'SpringAOP', title: 'Spring AOP', category: 'Spring' },
    { id: 'TransactionManagement', title: '事务管理', category: 'Spring' },
    { id: 'SpringSecurity', title: 'Spring Security', category: 'Spring' },
    { id: 'AutoConfiguration', title: '自动配置', category: 'Spring Boot' },
    { id: 'ServiceDiscovery', title: '服务发现', category: 'Spring Cloud' },
    { id: 'LoadBalancer', title: '负载均衡', category: 'Spring Cloud' },
    { id: 'CircuitBreaker', title: '熔断降级', category: 'Spring Cloud' },
    { id: 'ApiGateway', title: 'API网关', category: 'Spring Cloud' },
    { id: 'FeignClient', title: 'Feign客户端', category: 'Spring Cloud' },
    { id: 'NacosAndApollo', title: '配置中心', category: 'Spring Cloud' },
    { id: 'SentinelFlowControl', title: '流量控制', category: 'Spring Cloud' },
    { id: 'DubboRPC', title: 'Dubbo RPC', category: 'RPC' },
    { id: 'MyBatisUsage', title: 'MyBatis', category: 'ORM' },
    { id: 'MySQLIndex', title: 'MySQL索引', category: 'MySQL' },
    { id: 'TransactionIsolation', title: '事务隔离', category: 'MySQL' },
    { id: 'MVCC', title: 'MVCC', category: 'MySQL' },
    { id: 'Sharding', title: '分库分表', category: 'MySQL' },
    { id: 'ConnectionPool', title: '连接池', category: 'MySQL' },
    { id: 'RedisDataStructures', title: 'Redis数据结构', category: 'Redis' },
    { id: 'RedisHighAvailability', title: 'Redis高可用', category: 'Redis' },
    { id: 'DistributedLock', title: '分布式锁', category: '分布式' },
    { id: 'DistributedTransaction', title: '分布式事务', category: '分布式' },
    { id: 'RocketMQ', title: 'RocketMQ', category: '消息队列' },
    { id: 'Kafka', title: 'Kafka', category: '消息队列' },
    { id: 'RabbitMQ', title: 'RabbitMQ', category: '消息队列' },
    { id: 'MessageReliability', title: '消息可靠性', category: '消息队列' },
    { id: 'SortingAlgorithm', title: '排序算法', category: '算法' },
    { id: 'HashMapSource', title: 'HashMap源码', category: '数据结构' },
    { id: 'AdvancedDataStructures', title: '高级数据结构', category: '数据结构' },
    { id: 'TCPAndHTTP', title: 'TCP与HTTP', category: '网络' },
    { id: 'NettyNIO', title: 'Netty NIO', category: '网络' },
    { id: 'SystemDesign', title: '系统设计', category: '架构' },
    { id: 'DistributedTracing', title: '分布式追踪', category: '架构' },
    { id: 'DockerKubernetes', title: 'Docker与K8s', category: '架构' }
  ];

  // 获取当前页面ID
  function getCurrentPageId() {
    var path = window.location.pathname;
    var match = path.match(/\/knowledge\/([^/]+)\.html$/);
    return match ? match[1] : null;
  }

  // 获取当前页面索引
  function getCurrentPageIndex() {
    var currentId = getCurrentPageId();
    if (!currentId) return -1;
    for (var i = 0; i < pages.length; i++) {
      if (pages[i].id === currentId) return i;
    }
    return -1;
  }

  // 创建元素辅助函数
  function createElement(tag, className, text) {
    var el = document.createElement(tag);
    if (className) el.className = className;
    if (text !== undefined) el.textContent = text;
    return el;
  }

  // 生成页面目录大纲
  function generateTOC() {
    var container = document.querySelector('.container');
    if (!container) return null;

    // 查找所有 h1, h2, h3 标题
    var headings = container.querySelectorAll('h1, h2, h3');
    if (headings.length === 0) return null;

    var tocContainer = createElement('div', 'toc-container');
    var tocHeader = createElement('div', 'toc-header', '目录');
    tocContainer.appendChild(tocHeader);

    var tocList = createElement('ul', 'toc-list');
    var tocItems = [];
    var counters = [0, 0, 0]; // h1, h2, h3 计数器

    headings.forEach(function(heading, index) {
      // 为每个标题生成唯一 ID
      var id = heading.id || ('toc-heading-' + index);
      if (!heading.id) {
        heading.id = id;
      }

      var level = parseInt(heading.tagName.charAt(1));
      var text = heading.textContent.trim();

      // 跳过主标题（h1）
      if (level === 1 && index === 0) return;

      // 更新计数器
      counters[level - 1]++;
      // 重置更低级别的计数器
      for (var i = level; i < counters.length; i++) {
        counters[i] = 0;
      }

      // 生成序号
      var number = '';
      if (level === 2) {
        number = counters[1] + '. ';
      } else if (level === 3) {
        number = counters[1] + '.' + counters[2] + ' ';
      }

      var li = createElement('li', 'toc-item toc-level-' + level);
      var link = createElement('a', 'toc-link', number + text);
      link.href = '#' + id;
      link.dataset.target = id;

      link.addEventListener('click', function(e) {
        e.preventDefault();
        heading.scrollIntoView({ behavior: 'smooth', block: 'start' });
        history.pushState(null, null, '#' + id);
      });

      li.appendChild(link);
      tocList.appendChild(li);
      tocItems.push({ link: link, target: id, level: level });
    });

    if (tocList.children.length === 0) return null;

    tocContainer.appendChild(tocList);

    // 添加折叠/展开按钮
    var toggleBtn = createElement('button', 'toc-toggle', '◀');
    toggleBtn.title = '收起目录';
    toggleBtn.addEventListener('click', function() {
      tocContainer.classList.toggle('toc-collapsed');
      var isCollapsed = tocContainer.classList.contains('toc-collapsed');
      // 同步切换 body 类名，用于控制内容布局
      document.body.classList.toggle('toc-collapsed', isCollapsed);
      toggleBtn.textContent = isCollapsed ? '▶' : '◀';
      toggleBtn.title = isCollapsed ? '展开目录' : '收起目录';
      try {
        localStorage.setItem('toc-collapsed', isCollapsed ? '1' : '0');
      } catch (e) {}
    });
    tocContainer.appendChild(toggleBtn);

    // 恢复之前的折叠状态
    try {
      if (localStorage.getItem('toc-collapsed') === '1') {
        tocContainer.classList.add('toc-collapsed');
        document.body.classList.add('toc-collapsed');
        toggleBtn.textContent = '▶';
        toggleBtn.title = '展开目录';
      }
    } catch (e) {}

    // 滚动时高亮当前章节
    function updateActiveItem() {
      var scrollPos = window.scrollY + 100;
      var activeFound = false;

      // 从后往前找，找到第一个在视口上方的标题
      for (var i = tocItems.length - 1; i >= 0; i--) {
        var targetEl = document.getElementById(tocItems[i].target);
        if (targetEl) {
          var offsetTop = targetEl.offsetTop;
          if (offsetTop <= scrollPos && !activeFound) {
            tocItems[i].link.parentElement.classList.add('active');
            activeFound = true;
          } else {
            tocItems[i].link.parentElement.classList.remove('active');
          }
        }
      }
    }

    var scrollTimer;
    window.addEventListener('scroll', function() {
      if (scrollTimer) clearTimeout(scrollTimer);
      scrollTimer = setTimeout(updateActiveItem, 50);
    });

    // 初始化高亮
    setTimeout(updateActiveItem, 100);

    return tocContainer;
  }

  // 创建导航按钮容器
  function createNavButtons() {
    var currentIndex = getCurrentPageIndex();
    if (currentIndex === -1) return null;

    var container = createElement('div', 'nav-buttons-container');

    // 上一页按钮
    var prevBtn = createElement('a', 'nav-btn nav-btn-prev');
    if (currentIndex > 0) {
      var prevPage = pages[currentIndex - 1];
      prevBtn.href = prevPage.id + '.html';
      prevBtn.appendChild(createElement('span', 'nav-btn-icon', '←'));
      var prevText = createElement('span', 'nav-btn-text');
      prevText.appendChild(createElement('span', 'nav-btn-label', '上一页'));
      prevText.appendChild(createElement('span', 'nav-btn-title', prevPage.title));
      prevBtn.appendChild(prevText);
    } else {
      prevBtn.className += ' nav-btn-disabled';
      prevBtn.href = 'javascript:void(0)';
      prevBtn.appendChild(createElement('span', 'nav-btn-icon', '←'));
      var prevText = createElement('span', 'nav-btn-text');
      prevText.appendChild(createElement('span', 'nav-btn-label', '上一页'));
      prevText.appendChild(createElement('span', 'nav-btn-title', '没有了'));
      prevBtn.appendChild(prevText);
    }
    container.appendChild(prevBtn);

    // 中间：返回知识地图
    var homeBtn = createElement('a', 'nav-btn nav-btn-home');
    homeBtn.href = '../knowledge-map.html';
    homeBtn.appendChild(createElement('span', 'nav-btn-icon', '📚'));
    homeBtn.appendChild(createElement('span', 'nav-btn-text', '知识地图'));
    container.appendChild(homeBtn);

    // 下一页按钮
    var nextBtn = createElement('a', 'nav-btn nav-btn-next');
    if (currentIndex < pages.length - 1) {
      var nextPage = pages[currentIndex + 1];
      nextBtn.href = nextPage.id + '.html';
      var nextText = createElement('span', 'nav-btn-text');
      nextText.appendChild(createElement('span', 'nav-btn-label', '下一页'));
      nextText.appendChild(createElement('span', 'nav-btn-title', nextPage.title));
      nextBtn.appendChild(nextText);
      nextBtn.appendChild(createElement('span', 'nav-btn-icon', '→'));
    } else {
      nextBtn.className += ' nav-btn-disabled';
      nextBtn.href = 'javascript:void(0)';
      var nextText = createElement('span', 'nav-btn-text');
      nextText.appendChild(createElement('span', 'nav-btn-label', '下一页'));
      nextText.appendChild(createElement('span', 'nav-btn-title', '没有了'));
      nextBtn.appendChild(nextText);
      nextBtn.appendChild(createElement('span', 'nav-btn-icon', '→'));
    }
    container.appendChild(nextBtn);

    return container;
  }

  // 创建搜索框
  function createSearchBox() {
    var container = createElement('div', 'nav-search-container');

    var inputWrapper = createElement('div', 'nav-search-wrapper');

    var icon = createElement('span', 'nav-search-icon', '🔍');
    inputWrapper.appendChild(icon);

    var input = createElement('input', 'nav-search-input');
    input.type = 'text';
    input.placeholder = '搜索知识点...';
    input.autocomplete = 'off';
    inputWrapper.appendChild(input);

    var clearBtn = createElement('span', 'nav-search-clear');
    clearBtn.style.display = 'none';
    inputWrapper.appendChild(clearBtn);

    container.appendChild(inputWrapper);

    var dropdown = createElement('div', 'nav-search-dropdown');
    dropdown.style.display = 'none';
    container.appendChild(dropdown);

    // 搜索逻辑
    var selectedIndex = -1;
    var filteredPages = [];

    function updateDropdown() {
      dropdown.innerHTML = '';
      selectedIndex = -1;

      if (filteredPages.length === 0) {
        var noResult = createElement('div', 'nav-search-no-result');
        noResult.textContent = '没有找到相关知识点';
        dropdown.appendChild(noResult);
      } else {
        filteredPages.forEach(function(page, index) {
          var item = createElement('a', 'nav-search-item');
          item.href = 'knowledge/' + page.id + '.html';
          if (getCurrentPageId()) {
            item.href = page.id + '.html';
          }

          var title = createElement('span', 'nav-search-item-title');
          title.textContent = page.title;
          item.appendChild(title);

          var category = createElement('span', 'nav-search-item-category');
          category.textContent = page.category;
          item.appendChild(category);

          item.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = item.href;
          });

          dropdown.appendChild(item);
        });
      }
      dropdown.style.display = 'block';
    }

    function selectItem(index) {
      var items = dropdown.querySelectorAll('.nav-search-item');
      items.forEach(function(item, i) {
        if (i === index) {
          item.classList.add('selected');
          item.scrollIntoView({ block: 'nearest' });
        } else {
          item.classList.remove('selected');
        }
      });
      selectedIndex = index;
    }

    input.addEventListener('input', function() {
      var query = this.value.trim().toLowerCase();
      clearBtn.style.display = query ? 'block' : 'none';

      if (!query) {
        dropdown.style.display = 'none';
        return;
      }

      filteredPages = pages.filter(function(page) {
        return page.title.toLowerCase().includes(query) ||
               page.category.toLowerCase().includes(query) ||
               page.id.toLowerCase().includes(query);
      }).slice(0, 10);

      updateDropdown();
    });

    input.addEventListener('keydown', function(e) {
      var items = dropdown.querySelectorAll('.nav-search-item');

      if (e.key === 'ArrowDown') {
        e.preventDefault();
        if (dropdown.style.display === 'none') {
          filteredPages = pages.slice(0, 10);
          updateDropdown();
          selectItem(0);
        } else {
          selectItem((selectedIndex + 1) % items.length);
        }
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        if (dropdown.style.display !== 'none') {
          selectItem(selectedIndex <= 0 ? items.length - 1 : selectedIndex - 1);
        }
      } else if (e.key === 'Enter') {
        e.preventDefault();
        if (selectedIndex >= 0 && items[selectedIndex]) {
          items[selectedIndex].click();
        } else if (filteredPages.length > 0) {
          window.location.href = filteredPages[0].id + '.html';
        }
      } else if (e.key === 'Escape') {
        dropdown.style.display = 'none';
        input.blur();
      }
    });

    clearBtn.addEventListener('click', function() {
      input.value = '';
      clearBtn.style.display = 'none';
      dropdown.style.display = 'none';
      input.focus();
    });

    // 点击外部关闭下拉框
    document.addEventListener('click', function(e) {
      if (!container.contains(e.target)) {
        dropdown.style.display = 'none';
      }
    });

    // 快捷键 / 聚焦搜索框
    document.addEventListener('keydown', function(e) {
      if (e.key === '/' && document.activeElement !== input) {
        e.preventDefault();
        input.focus();
      }
    });

    return container;
  }

  // 添加样式
  function addStyles() {
    if (document.getElementById('nav-styles')) return;

    var style = createElement('style', '');
    style.id = 'nav-styles';
    style.textContent = [
      /* CSS 变量定义 - 确保子页面有正确的颜色 */
      ':root {',
      '  --bg-deep: #0a0e17;',
      '  --bg-card: rgba(16, 22, 36, 0.7);',
      '  --border-card: rgba(240, 180, 41, 0.12);',
      '  --amber: #f0b429;',
      '  --amber-dim: rgba(240, 180, 41, 0.08);',
      '  --cyan: #4ecdc4;',
      '  --cyan-dim: rgba(78, 205, 196, 0.15);',
      '  --text-primary: #e8e6e3;',
      '  --text-secondary: #8a8f98;',
      '  --text-dim: #4a5068;',
      '  --glow-amber: 0 0 30px rgba(240, 180, 41, 0.15);',
      '}',
      '',
      '/* 亮色模式变量 */',
      '[data-theme="light"] {',
      '  --bg-deep: #f8f6f1;',
      '  --bg-card: rgba(255, 255, 255, 0.85);',
      '  --border-card: rgba(160, 120, 20, 0.18);',
      '  --amber: #b8860b;',
      '  --amber-dim: rgba(184, 134, 11, 0.06);',
      '  --cyan: #0d7377;',
      '  --cyan-dim: rgba(13, 115, 119, 0.12);',
      '  --text-primary: #1a1a2e;',
      '  --text-secondary: #4a4a5e;',
      '  --text-dim: #7a7a8e;',
      '  --glow-amber: 0 0 20px rgba(184, 134, 11, 0.08);',
      '}',
      '',
      '/* 导航按钮容器 */',
      '.nav-buttons-container {',
      '  display: flex;',
      '  justify-content: space-between;',
      '  align-items: center;',
      '  gap: 16px;',
      '  margin: 40px 0;',
      '  padding: 20px;',
      '  background: var(--bg-card);',
      '  border: 1px solid var(--border-card);',
      '  border-radius: 12px;',
      '  backdrop-filter: blur(12px);',
      '}',

      /* 导航按钮 */
      '.nav-btn {',
      '  display: flex;',
      '  align-items: center;',
      '  gap: 12px;',
      '  padding: 12px 20px;',
      '  background: var(--bg-card);',
      '  border: 1px solid var(--border-card);',
      '  border-radius: 10px;',
      '  text-decoration: none;',
      '  color: var(--text-primary);',
      '  transition: all 0.3s ease;',
      '  flex: 1;',
      '  max-width: 200px;',
      '}',

      '.nav-btn:hover:not(.nav-btn-disabled) {',
      '  border-color: var(--amber);',
      '  transform: translateY(-2px);',
      '  box-shadow: var(--glow-amber);',
      '}',

      '.nav-btn-disabled {',
      '  opacity: 0.5;',
      '  cursor: not-allowed;',
      '  pointer-events: none;',
      '}',

      '.nav-btn-prev { justify-content: flex-start; }',
      '.nav-btn-next { justify-content: flex-end; text-align: right; }',
      '.nav-btn-home { justify-content: center; flex: 0 0 auto; }',

      '.nav-btn-icon {',
      '  font-size: 18px;',
      '  flex-shrink: 0;',
      '}',

      '.nav-btn-text {',
      '  display: flex;',
      '  flex-direction: column;',
      '  gap: 2px;',
      '}',

      '.nav-btn-label {',
      '  font-size: 11px;',
      '  color: var(--text-dim);',
      '  text-transform: uppercase;',
      '  letter-spacing: 1px;',
      '}',

      '.nav-btn-title {',
      '  font-size: 14px;',
      '  font-weight: 600;',
      '  color: var(--amber);',
      '}',

      /* 搜索框容器 */
      '.nav-search-container {',
      '  position: fixed;',
      '  top: 20px;',
      '  left: 50%;',
      '  transform: translateX(-50%);',
      '  z-index: 1000;',
      '  width: 100%;',
      '  max-width: 400px;',
      '}',

      '.nav-search-wrapper {',
      '  display: flex;',
      '  align-items: center;',
      '  gap: 10px;',
      '  padding: 10px 16px;',
      '  background: var(--bg-card);',
      '  border: 1px solid var(--border-card);',
      '  border-radius: 10px;',
      '  backdrop-filter: blur(12px);',
      '  transition: all 0.3s ease;',
      '}',

      '.nav-search-wrapper:focus-within {',
      '  border-color: var(--amber);',
      '  box-shadow: var(--glow-amber);',
      '}',

      '.nav-search-icon {',
      '  font-size: 16px;',
      '  opacity: 0.7;',
      '}',

      '.nav-search-input {',
      '  flex: 1;',
      '  background: transparent;',
      '  border: none;',
      '  outline: none;',
      '  color: var(--text-primary);',
      '  font-family: var(--font-heading);',
      '  font-size: 14px;',
      '}',

      '.nav-search-input::placeholder {',
      '  color: var(--text-dim);',
      '}',

      '.nav-search-clear {',
      '  width: 20px;',
      '  height: 20px;',
      '  display: inline-block;',
      '  position: relative;',
      '  border-radius: 50%;',
      '  background: var(--border-card);',
      '  color: var(--text-secondary);',
      '  font-size: 16px;',
      '  line-height: 20px;',
      '  text-align: center;',
      '  cursor: pointer;',
      '  transition: all 0.2s;',
      '  vertical-align: middle;',
      '}',
      '.nav-search-clear::before {',
      '  content: "×";',
      '  position: absolute;',
      '  top: 50%;',
      '  left: 50%;',
      '  transform: translate(-50%, -50%);',
      '}',

      '.nav-search-clear:hover {',
      '  background: var(--amber);',
      '  color: var(--bg-deep);',
      '}',

      /* 搜索下拉框 */
      '.nav-search-dropdown {',
      '  position: absolute;',
      '  top: calc(100% + 8px);',
      '  left: 0;',
      '  right: 0;',
      '  max-height: 300px;',
      '  overflow-y: auto;',
      '  background: var(--bg-card);',
      '  border: 1px solid var(--border-card);',
      '  border-radius: 10px;',
      '  backdrop-filter: blur(12px);',
      '  box-shadow: var(--glow-amber);',
      '}',

      '.nav-search-item {',
      '  display: flex;',
      '  justify-content: space-between;',
      '  align-items: center;',
      '  padding: 12px 16px;',
      '  text-decoration: none;',
      '  color: var(--text-primary);',
      '  border-bottom: 1px solid var(--border-card);',
      '  transition: all 0.2s;',
      '}',

      '.nav-search-item:last-child {',
      '  border-bottom: none;',
      '}',

      '.nav-search-item:hover,',
      '.nav-search-item.selected {',
      '  background: var(--amber-dim);',
      '}',

      '.nav-search-item-title {',
      '  font-weight: 600;',
      '  font-size: 14px;',
      '}',

      '.nav-search-item-category {',
      '  font-size: 11px;',
      '  color: var(--cyan);',
      '  background: var(--cyan-dim);',
      '  padding: 2px 8px;',
      '  border-radius: 4px;',
      '}',

      '.nav-search-no-result {',
      '  padding: 20px;',
      '  text-align: center;',
      '  color: var(--text-dim);',
      '  font-size: 14px;',
      '}',

      '/* 目录大纲样式 */',
      '.toc-container {',
      '  position: fixed;',
      '  top: 80px;',
      '  right: 20px;',
      '  width: 220px;',
      '  max-height: calc(100vh - 120px);',
      '  background: var(--bg-card);',
      '  border: 1px solid var(--border-card);',
      '  border-radius: 12px;',
      '  backdrop-filter: blur(12px);',
      '  z-index: 100;',
      '  overflow: hidden;',
      '  transition: all 0.3s ease;',
      '}',

      '/* 页面内容留出右侧空间 */',
      '.container {',
      '  max-width: calc(100% - 320px) !important;',
      '  margin-right: 260px !important;',
      '  margin-left: 60px !important;',
      '  box-sizing: border-box;',
      '}',

      '/* 目录收缩时内容居中，宽度不变 */',
      'body.toc-collapsed .container {',
      '  margin-left: auto !important;',
      '  margin-right: auto !important;',
      '}',

      '.toc-container.toc-collapsed {',
      '  width: 40px;',
      '  height: 40px;',
      '  min-height: 40px;',
      '  max-height: 40px;',
      '  border-radius: 50%;',
      '  overflow: hidden;',
      '}',

      '.toc-container.toc-collapsed .toc-header,',
      '.toc-container.toc-collapsed .toc-list {',
      '  opacity: 0;',
      '  pointer-events: none;',
      '}',

      '.toc-header {',
      '  padding: 16px 20px 12px;',
      '  font-size: 14px;',
      '  font-weight: 700;',
      '  color: var(--amber);',
      '  text-transform: uppercase;',
      '  letter-spacing: 1px;',
      '  border-bottom: 1px solid var(--border-card);',
      '}',

      '.toc-toggle {',
      '  position: absolute;',
      '  top: 12px;',
      '  right: 12px;',
      '  width: 24px;',
      '  height: 24px;',
      '  border: none;',
      '  background: var(--border-card);',
      '  color: var(--text-secondary);',
      '  border-radius: 50%;',
      '  cursor: pointer;',
      '  font-size: 10px;',
      '  display: flex;',
      '  align-items: center;',
      '  justify-content: center;',
      '  transition: all 0.2s ease;',
      '}',

      '.toc-toggle:hover {',
      '  background: var(--amber);',
      '  color: var(--bg-deep);',
      '}',

      '.toc-list {',
      '  list-style: none;',
      '  padding: 8px 0;',
      '  margin: 0;',
      '  max-height: calc(100vh - 200px);',
      '  overflow-y: auto;',
      '  transition: opacity 0.2s ease;',
      '}',

      '.toc-list::-webkit-scrollbar {',
      '  width: 4px;',
      '}',

      '.toc-list::-webkit-scrollbar-track {',
      '  background: transparent;',
      '}',

      '.toc-list::-webkit-scrollbar-thumb {',
      '  background: var(--border-card);',
      '  border-radius: 2px;',
      '}',

      '.toc-item {',
      '  margin: 0;',
      '  padding: 0;',
      '}',

      '.toc-item.active > .toc-link {',
      '  color: var(--amber);',
      '  background: var(--amber-dim);',
      '  border-right-color: var(--amber);',
      '  border-left-color: transparent;',
      '}',

      '.toc-link {',
      '  display: block;',
      '  padding: 8px 16px 8px 12px;',
      '  color: var(--text-secondary);',
      '  text-decoration: none;',
      '  font-size: 13px;',
      '  line-height: 1.5;',
      '  border-right: 3px solid transparent;',
      '  border-left: none;',
      '  transition: all 0.2s ease;',
      '  white-space: nowrap;',
      '  overflow: hidden;',
      '  text-overflow: ellipsis;',
      '  text-align: left;',
      '}',

      '.toc-link:hover {',
      '  color: var(--text-primary);',
      '  background: var(--bg-card-hover);',
      '  border-right-color: var(--cyan);',
      '  border-left-color: transparent;',
      '}',

      '.toc-level-2 > .toc-link {',
      '  padding-left: 12px;',
      '}',

      '.toc-level-3 > .toc-link {',
      '  padding-left: 12px;',
      '  font-size: 12px;',
      '}',

      '/* 响应式：小屏幕隐藏目录 */',
      '@media (max-width: 1200px) {',
      '  .toc-container {',
      '    display: none;',
      '  }',
      '}',

      '/* 响应式 */',
      '@media (max-width: 768px) {',
      '  .nav-buttons-container {',
      '    flex-wrap: wrap;',
      '    gap: 12px;',
      '  }',
      '  .nav-btn {',
      '    flex: 1 1 calc(50% - 6px);',
      '    max-width: none;',
      '  }',
      '  .nav-btn-home {',
      '    flex: 1 1 100%;',
      '    order: -1;',
      '  }',
      '  .nav-search-container {',
      '    max-width: calc(100% - 120px);',
      '    left: 20px;',
      '    transform: none;',
      '  }',
      '}'
    ].join('\n');

    document.head.appendChild(style);
  }

  // 应用保存的主题设置
  function applySavedTheme() {
    try {
      var savedTheme = localStorage.getItem('theme');
      if (savedTheme === 'light') {
        document.documentElement.setAttribute('data-theme', 'light');
        var toggleBtn = document.getElementById('themeToggle');
        if (toggleBtn) toggleBtn.textContent = '☀️';
      } else if (savedTheme === 'dark') {
        document.documentElement.removeAttribute('data-theme');
        var toggleBtn = document.getElementById('themeToggle');
        if (toggleBtn) toggleBtn.textContent = '🌙';
      }
    } catch (e) {
      // localStorage 不可用（如隐私模式）
    }
  }

  // 增强主题切换按钮，添加 localStorage 持久化
  function enhanceThemeToggle() {
    var toggleBtn = document.getElementById('themeToggle');
    if (!toggleBtn) return;

    // 移除原有事件监听（通过克隆按钮）
    var newBtn = toggleBtn.cloneNode(true);
    toggleBtn.parentNode.replaceChild(newBtn, toggleBtn);

    newBtn.addEventListener('click', function() {
      var html = document.documentElement;
      if (html.getAttribute('data-theme') === 'light') {
        html.removeAttribute('data-theme');
        newBtn.textContent = '🌙';
        try { localStorage.setItem('theme', 'dark'); } catch (e) {}
      } else {
        html.setAttribute('data-theme', 'light');
        newBtn.textContent = '☀️';
        try { localStorage.setItem('theme', 'light'); } catch (e) {}
      }
    });
  }

  // 初始化导航
  function init() {
    // 首先应用保存的主题（在添加其他元素之前）
    applySavedTheme();

    addStyles();

    // 增强主题切换按钮
    enhanceThemeToggle();

    // 添加搜索框到页面（所有页面）
    var searchBox = createSearchBox();
    document.body.appendChild(searchBox);

    // 添加目录大纲和导航按钮（仅在知识页面）
    var currentPageId = getCurrentPageId();
    if (currentPageId) {
      var toc = generateTOC();
      if (toc) {
        document.body.appendChild(toc);
      }

      var navButtons = createNavButtons();
      if (navButtons) {
        // 找到合适的位置插入（在 footer 之前或在 container 末尾）
        var container = document.querySelector('.container');
        var footer = document.querySelector('.footer, .kp-footer');
        if (footer && footer.parentNode) {
          footer.parentNode.insertBefore(navButtons, footer);
        } else if (container) {
          container.appendChild(navButtons);
        }
      }
    }
  }

  // DOM 加载完成后初始化
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
