## Continuous integration

### Build and test
Swagger Validator uses Github actions to run jobs/checks building and testing on push and PR events. Nothing is published to Maven Central: the WAR is no longer published there, and the Docker image `swaggerapi/swagger-validator-v2` is the release artifact.

These github actions are configured in `.github/workflows`:

* maven.yml : Build Test master
* maven-pulls.yml Build Test PR


These actions use available actions in combination with short bash scripts.

### Release

Releases are semi-automated and consist in 2 actions using available public actions in combination with bash and python scripts.
**TODO**: Python code is used for historical reasons to execute GitHub APIs calls, in general a more consistent environment would
be more maintainable e.g. implementing a custom JavaScript or Docker Container GitHub Action and/or a bash only script(s).

#### Workflow summary

1. execute `prepare-release.yml` / `Prepare Release` for `master` branch
1. check and merge the Prepare Release PR pushed by previous step. Delete the branch
1. execute `release.yml` / `Release` for `master` branch
1. check and merge the next snaphot PR pushed by previous step. Delete the branch

#### Prepare Release

The first action to execute is `prepare-release.yml` / `Prepare Release`

This is triggered by manually executing the action, selecting `Actions` in project GitHub UI, then `Prepare Release` workflow
and clicking `Run Workflow` 

`Prepare Release` takes care of:

* create release notes out of merged PRs
* Draft a release with related tag
* bump versions to release, and update all affected files
* build and test maven
* push a Pull Request with the changes for human check.

After the PR checks complete, the PR can me merged, and the second phase `Release` started.

#### Release

Once prepare release PR has been merged, the second phase is provided by `release.yml` / `Release` actions.

This is triggered by manually executing the action, selecting `Actions` in project GitHub UI, then `Release` workflow
and clicking `Run Workflow` 

`Release` takes care of:

* build and test maven
* publish the previously prepared GitHub release / tag
* build and push docker image to docker hub (the release artifact)
* deploy the docker image to Kubernetes (EKS)
* push PR for next snapshot



### Secrets

GitHub Actions make use of `Secrets` which can be configured either with Repo or Organization scope; the needed secrets are the following:

* `APP_ID` and APP_PRIVATE_KEY`: these are the values provided by an account configured GitHub App, allowing to obtain a GitHub token
different from the default used in GitHub Actions (which does not allow to "chain" actions).Actions

The GitHub App must be configured as detailed in [this doc](https://github.com/peter-evans/create-pull-request/blob/master/docs/concepts-guidelines.md#authenticating-with-github-app-generated-tokens).

See also [here](https://github.com/peter-evans/create-pull-request/blob/master/docs/concepts-guidelines.md#triggering-further-workflow-runs)
