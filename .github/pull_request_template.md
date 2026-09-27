\## User Story



<!--

Reference to the story in telemed-ia-docs:

code-corhuila/telemed-ia-docs#NN

If the PR is for infrastructure or tooling, write N/A and explain why.



\--

\## What Changes and Why



<!--

In 3-5 lines: the problem this PR solves and the decision that was made.

If the change is purely technical, state so.



\--

\## How It Was Tested



<!--

\- Which tests cover the change.



\- Result of the `ci.yml` flow (green/red).



\- Local commands executed and their result.



\--

\## Promotion Trace



<!--

Only for PRs to `qa` or `main`.



List each re-applied commit with its traceability line:



\- chore(api): split maven project into core, adapters, and app modules



\- cherry picked from commit <source-sha>

\- feat(api): validate RS256 JWT in service instead of shared gateway secret



\- cherry picked from commit <source-sha>



For PRs to `develop`, write: N/A — PR to develop.



\->



N/A — PR to `develop`.



\## Checklist



\- \[ ] No secrets or real versioned credentials.



\- \[ ] No schema changes outside the `-db` repository.



\- \[ ] The public contract is respected (or extended without breaking).



\- \[ ] The three permanent branches remain intact.



\- \[ ] Commit messages follow Conventional Commits.



\- \[ ] The CI is green (`mvn -B verify`).



\- \[ ] Updated affected documentation.

