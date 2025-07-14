# generic-webhook

## Introduction

Generic Webhook is a simple Jenkins plugin that exposes a public endpoint to
trigger pipelines from external systems. Requests are sent to
`/generic-webhook/{pipelineName}` using the `POST` method with any JSON body.
The JSON payload is forwarded to the triggered pipeline as a parameter named
`webhookPayload`.

## Getting started

Configure your pipeline to declare a string parameter called `webhookPayload`.
Then send a `POST` request to `/generic-webhook/<job name>` with your JSON
payload. Inside the pipeline access it via `params.webhookPayload`.

## Issues

TODO Decide where you're going to host your issues, the default is Jenkins JIRA, but you can also enable GitHub issues,
If you use GitHub issues there's no need for this section; else add the following line:

Report issues and enhancements in the [Jenkins issue tracker](https://issues.jenkins.io/).

## Contributing

TODO review the default [CONTRIBUTING](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md) file and make sure it is appropriate for your plugin, if not then add your own one adapted from the base file

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

