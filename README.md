# Zally: A minimalistic, simple-to-use OpenAPI 2 and 3 linter

[![Build Status](https://travis-ci.org/zalando/zally.svg?branch=master)](https://travis-ci.org/zalando/zally)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/05a7515011504c06b1cb35ede27ac7d4)](https://www.codacy.com/app/zally/zally?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=zalando/zally&amp;utm_campaign=Badge_Grade)
[![Join the chat at https://gitter.im/zalando/zally](https://badges.gitter.im/zalando/zally.svg)](https://gitter.im/zalando/zally?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

<img src="logo.png" width="200" height="200" />

Zally is a quality assurance tool. It's a linter for OpenAPI specifications, which:

- Increases the quality of APIs
- Checks compliance
- Delivers early feedback for API designers
- Ensures the same look-and-feel of APIs
- Supports [API-First approach](https://opensource.zalando.com/restful-api-guidelines/#100)
- Provides best practices and advices

Its standard configuration will check your APIs against the rules defined in
[Zalando's RESTful Guidelines](http://zalando.github.io/restful-api-guidelines/),
but anyone can use it **out-of-the-box**.

Zally's easy-to-use [CLI](cli/README.md) uses the server in the background so that
you can check your API *on the spot*. It also features an intuitive
[Web UI](web-ui/README.md) that shows implemented rules and lints external files
and (with its online editor) API definitions.

## Features

- Support for OpenAPI 3 and (Swagger) OpenAPI 2 specifications
- RESTful API, CLI and Web interface
- Rich Check configuration
- Ignore functionality (`x-zally-ignore` extension)
- Java/Kotlin API for new Checks + helper functions

## Quick start guide

Trying out Zally is easy. You can build and run the whole Zally stack (web-ui, server
and database) by executing:

```bash
./build-and-run.sh
```

Web UI is accessible on `http://localhost:8080`; Zally server on `http://localhost:8000`

## Documentation and Manuals

Please consult the following documents for more information:

- [Zally Concepts](documentation/concepts.md)
- [How to operate](documentation/operation.md) Zally tools
- [How to use Zally](documentation/usage.md)
- [How to develop new Rules](documentation/rule-development.md)
- [Building Under Windows Subsystem for Linux](documentation/build-under-wsl.md)

## Contributing

Zally welcomes contributions from the open source community. To get started, take a
look at our [contributing guidelines](CONTRIBUTING). Then check our
[Project Board](https://github.com/zalando/zally/projects/1) and
[Issues Tracker](https://github.com/zalando/zally/issues) for ideas.

## Roadmap

For Zally [version 1.5](https://github.com/zalando/zally/milestone/3), we're focusing on:

- Improve extensibility of Zally
  - Plugin mechanism for Rules (Sets)
  - Utilities and helper functions for Check development
- Improve quality by introducing a better testing approach for integration tests
- Make the usage of Zally easier by providing high-quality documentation for
  - End users
  - Check developers
  - Operators and administrators

If you have ideas for these items, please let us know.

## Contact

Feel free to join our [Gitter room](https://gitter.im/zalando/zally) or contact one
of the [maintainers](MAINTAINERS) directly.

## License

MIT license with an exception. See [license file](LICENSE).
