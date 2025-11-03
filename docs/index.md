dd-vault-catalog
================

Catalog of the contents of the DANS Data Vault

Purpose
-------
This service provides a catalog of the contents of the DANS Data Vault. All datasets stored in all [Data Vault Storage Root]{:target=_blank}s is represented by
a metadata record in the catalog. A catalog summary can be retrieved for each dataset. 

Interfaces
----------

### Provided interfaces

#### API

* _Protocol type_: HTTP
* _Internal or external_: **internal**
* _Purpose_: to add and retrieve dataset versions.

#### Web UI
* _Protocol type_: HTTP
* _Internal or external_: **external**
* _Purpose_: to serve catalog summary pages for datasets.

#### Admin console

* _Protocol type_: HTTP
* _Internal or external_: **internal**
* _Purpose_: to monitor and manage the catalog service.

[Data Vault Storage Root]: {{ data_vault_storage_root_url }}