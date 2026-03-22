#!/usr/bin/env python3
"""Update knowledge-map.html to add links to sub-cards"""

import re

def update_subcards(html_content):
    """Convert sub-card divs to anchor links"""

    # Pattern to match sub-card divs
    pattern = r'<div class="sub-card"><div class="sub-card-title">(\w+)</div><div class="sub-card-desc">([^<]+)</div></div>'

    def replace_with_link(match):
        class_name = match.group(1)
        desc = match.group(2)
        return f'<a href="knowledge/{class_name}.html" class="sub-card"><div class="sub-card-title">{class_name}</div><div class="sub-card-desc">{desc}</div></a>'

    return re.sub(pattern, replace_with_link, html_content)


def update_css(html_content):
    """Add CSS for anchor sub-cards"""
    css_addition = '''
  .sub-card {
    display: block;
    text-decoration: none;
    color: inherit;
  }
'''
    # Insert before the first style rule
    return html_content.replace('.sub-card {', css_addition + '  .sub-card {')


def main():
    with open('knowledge-map.html', 'r', encoding='utf-8') as f:
        content = f.read()

    # Update sub-cards to links
    content = update_subcards(content)

    # Update CSS
    content = update_css(content)

    with open('knowledge-map.html', 'w', encoding='utf-8') as f:
        f.write(content)

    print("Updated knowledge-map.html with links")


if __name__ == '__main__':
    main()
