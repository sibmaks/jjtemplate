const root = document.documentElement;
const themeButton = document.querySelector('#theme-toggle');
const menuButton = document.querySelector('#menu-button');
const mobileNav = document.querySelector('#mobile-nav');

const storedTheme = localStorage.getItem('jjt-theme');
if (storedTheme === 'light' || storedTheme === 'dark') {
  root.dataset.theme = storedTheme;
} else if (window.matchMedia('(prefers-color-scheme: light)').matches) {
  root.dataset.theme = 'light';
}

themeButton.addEventListener('click', () => {
  const nextTheme = root.dataset.theme === 'dark' ? 'light' : 'dark';
  root.dataset.theme = nextTheme;
  localStorage.setItem('jjt-theme', nextTheme);
});

menuButton.addEventListener('click', () => {
  const open = mobileNav.classList.toggle('is-open');
  menuButton.setAttribute('aria-expanded', String(open));
});

mobileNav.querySelectorAll('a').forEach((link) => {
  link.addEventListener('click', () => {
    mobileNav.classList.remove('is-open');
    menuButton.setAttribute('aria-expanded', 'false');
  });
});

document.querySelectorAll('[data-copy-target]').forEach((button) => {
  button.addEventListener('click', async () => {
    const target = document.getElementById(button.dataset.copyTarget);
    if (!target) return;

    try {
      await navigator.clipboard.writeText(target.innerText);
      const previousLabel = button.textContent;
      button.textContent = 'Copied';
      button.classList.add('is-copied');
      window.setTimeout(() => {
        button.textContent = previousLabel;
        button.classList.remove('is-copied');
      }, 1400);
    } catch {
      button.textContent = 'Select text';
    }
  });
});

document.querySelectorAll('[data-tab-group]').forEach((tab) => {
  tab.addEventListener('click', () => {
    const group = tab.dataset.tabGroup;
    document.querySelectorAll(`[data-tab-group="${group}"]`).forEach((candidate) => {
      const active = candidate === tab;
      candidate.classList.toggle('is-active', active);
      candidate.setAttribute('aria-selected', String(active));
    });
    document.querySelectorAll(`[data-tab-panel^="${group}:"]`).forEach((panel) => {
      panel.classList.toggle('is-active', panel.dataset.tabPanel === `${group}:${tab.dataset.tab}`);
    });
  });
});

const examples = {
  conditional: {
    input: `{
  "definitions": [
    {
      "primary": "available",
      "secondary": null
    }
  ],
  "template": {
    "values": [
      "{{? .primary }}",
      "{{? .secondary }}"
    ]
  }
}`,
    output: `{
  "values": [
    "available"
  ]
}`
  },
  spread: {
    input: `{
  "definitions": [
    { "extra": { "role": "admin" } }
  ],
  "template": {
    "name": "Ada",
    "{{. .extra}}": true
  }
}`,
    output: `{
  "name": "Ada",
  "role": "admin"
}`
  },
  range: {
    input: `{
  "definitions": [
    {
      "items": ["A", "B", "C"],
      "{{ rows range item,index of .items }}": {
        "position": "{{ .index }}",
        "value": "{{ .item }}"
      }
    }
  ],
  "template": "{{ .rows }}"
}`,
    output: `[
  { "position": 0, "value": "A" },
  { "position": 1, "value": "B" },
  { "position": 2, "value": "C" }
]`
  },
  switch: {
    input: `{
  "definitions": [
    { "status": "fail" },
    {
      "{{ message switch .status }}": {
        "ok": "All good",
        "fail": "Something went wrong",
        "{{ else }}": "Unknown"
      }
    }
  ],
  "template": { "message": "{{ .message }}" }
}`,
    output: `{
  "message": "Something went wrong"
}`
  }
};

const inputExample = document.querySelector('#example-input');
const outputExample = document.querySelector('#example-output');

function highlightJson(source, target) {
  const fragment = document.createDocumentFragment();
  const tokenPattern = /"(?:\\.|[^"\\])*"|-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?|\b(?:true|false|null)\b|[{}[\],:]/g;
  let cursor = 0;

  source.replace(tokenPattern, (token, offset) => {
    if (offset > cursor) {
      fragment.append(document.createTextNode(source.slice(cursor, offset)));
    }

    const span = document.createElement('span');
    if (token.startsWith('"')) {
      const isKey = source.slice(offset + token.length).trimStart().startsWith(':');
      span.className = token.includes('{{') ? 'tok-expr' : isKey ? 'tok-key' : 'tok-string';
    } else if (/^-?\d/.test(token)) {
      span.className = 'tok-number';
    } else if (/^(?:true|false|null)$/.test(token)) {
      span.className = 'tok-bool';
    } else {
      span.className = 'tok-punc';
    }
    span.textContent = token;
    fragment.append(span);
    cursor = offset + token.length;
    return token;
  });

  if (cursor < source.length) {
    fragment.append(document.createTextNode(source.slice(cursor)));
  }
  target.replaceChildren(fragment);
}

function renderExample(name) {
  const example = examples[name];
  highlightJson(example.input, inputExample);
  highlightJson(example.output, outputExample);
}

document.querySelectorAll('[data-example]').forEach((button) => {
  button.addEventListener('click', () => {
    document.querySelectorAll('[data-example]').forEach((candidate) => {
      const active = candidate === button;
      candidate.classList.toggle('is-active', active);
      candidate.setAttribute('aria-selected', String(active));
    });
    renderExample(button.dataset.example);
  });
});

renderExample('conditional');

const searchInput = document.querySelector('#function-search');
const filterButtons = [...document.querySelectorAll('[data-filter]')];
const functionCards = [...document.querySelectorAll('.function-card')];
const functionEmpty = document.querySelector('#function-empty');
let activeFilter = 'all';

function filterFunctions() {
  const query = searchInput.value.trim().toLowerCase();
  let visibleCount = 0;

  functionCards.forEach((card) => {
    const categoryMatches = activeFilter === 'all' || card.dataset.category === activeFilter;
    const queryMatches = !query || card.dataset.search.toLowerCase().includes(query) || card.innerText.toLowerCase().includes(query);
    const visible = categoryMatches && queryMatches;
    card.hidden = !visible;
    if (visible) visibleCount += 1;
  });

  functionEmpty.hidden = visibleCount !== 0;
}

filterButtons.forEach((button) => {
  button.addEventListener('click', () => {
    activeFilter = button.dataset.filter;
    filterButtons.forEach((candidate) => candidate.classList.toggle('is-active', candidate === button));
    filterFunctions();
  });
});

searchInput.addEventListener('input', filterFunctions);

const observedSections = [...document.querySelectorAll('main section[id]')];
const navLinks = [...document.querySelectorAll('.topnav a')];
const observer = new IntersectionObserver((entries) => {
  const visible = entries
    .filter((entry) => entry.isIntersecting)
    .sort((left, right) => right.intersectionRatio - left.intersectionRatio)[0];
  if (!visible) return;

  navLinks.forEach((link) => {
    link.classList.toggle('is-active', link.getAttribute('href') === `#${visible.target.id}`);
  });
}, { rootMargin: '-20% 0px -65% 0px', threshold: [0.05, 0.2, 0.5] });

observedSections.forEach((section) => observer.observe(section));
