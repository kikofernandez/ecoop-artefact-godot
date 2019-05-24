# README

The documentation is generated from the markdown document `INSTRUCTIONS.md`.
This repository contains the (self-contained) generated versions:

- `INSTRUCTIONS.html` (recommended for reading)
- `INSTRUCTIONS.pdf`

If you want to generate the instructions in other format, please install
`pandoc` and update the `Makefile` accordingly. For example, generating a Word
document would be as simple as typing:

```bash
pandoc -s $(META) $(DOCS) --self-contained -c assets/pandoc.css -o $(DOCS:md=docx)
```

## Instructions

Please look at the instructions on how to use this artefact from either
`INSTRUCTIONS.html` (recommended for reading) or `INSTRUCTIONS.pdf`.

Enjoy!
