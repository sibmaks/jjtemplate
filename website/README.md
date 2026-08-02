# JJTemplate documentation site

This directory contains the JJTemplate Jekyll site published through GitHub
Pages. The site intentionally uses a custom, dependency-free layout while
Jekyll provides the Pages build and deployment boundary.

## Preview locally

Open `index.html` directly or serve this directory with any static HTTP server.
The page uses relative asset URLs, so both direct preview and project Pages work.

## Publish with GitHub Actions

The workflow in `.github/workflows/pages.yml` builds this directory with Jekyll
and deploys the generated artifact. It runs for website changes on `main`, can
be started manually, and validates the Jekyll build in pull requests.

Before the first deployment, open **Settings → Pages** and select **GitHub
Actions** as the source. After that, merge a website change to `main` or run
the **Deploy website to GitHub Pages** workflow from the Actions tab.

## Files

- `index.html` — content and semantic structure;
- `styles.css` — responsive layout and light/dark themes;
- `app.js` — navigation, copy buttons, examples, and function filtering;
- `favicon.svg` — browser icon matching the site brand mark;
- `assets/og.png` — social preview image;
- `_config.yml` — Jekyll and GitHub Pages metadata.

`index.html` intentionally has no front matter, so Jekyll copies it verbatim.
That prevents Liquid from interpreting JJTemplate's own `{{ ... }}` examples.
