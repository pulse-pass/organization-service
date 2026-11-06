---
description: Status change and logical removal routes
paths:
  - "**/api/**"
  - "**/application/**"
---

# Status routes

- **ORG-063** Status change route: `PATCH /<service>/internal/<resources>/{<resource>_key}` takes `<x>_status` and accepts only the allowed target statuses (a schema enum in `definitions/`).
- **ORG-064** A status change is accepted only from the allowed source status, otherwise 422 with the status-not-allowed exception.
- **ORG-065** An option tied to the target (e.g. the rejection reason) is validated in the service and answers 422 with its own exception when missing or not allowed.
- **ORG-066** The response carries only the key, the status and the fields tied to it (e.g. the rejection reason and its description), with no timestamps.
- **ORG-067** Logical removal is `DELETE` on the same item (status `DELETED`, only from the allowed source status), answering 204 without a body.
- **ORG-068** Both routes look the item up by key with the lock (`findForUpdateBy<Key>`, the not-found exception answering 404) and check the source status before the option, inside the transaction.
- **ORG-069** Every change writes one `<x>_history` row with the old status.
