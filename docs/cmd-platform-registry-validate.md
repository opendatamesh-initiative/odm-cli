# odmcli platform registry validate

## Usage

`odm-cli platform registry validate`

## Description

This command validates a data product descriptor against the policies registered on the ODM Platform. It also checks for
syntax errors within the descriptor file, ensuring compliance with required standards before registration.

**Note:** To function correctly, the CLI must be properly configured and have access to the Registry Service REST API
endpoints.

## Options

| Option           | Default                           | Description                                                                                                                                                                             |
|------------------|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-f, --file`     | PATH/data-product-descriptor.json | Name of the descriptor file to validate                                                                                                                                                 |
| `-e, --event`    | All event                         | If set, the descriptor is validated only against policies that are triggered by the given event. (e.g. `DATA_PRODUCT_CREATION`, `DATA_PRODUCT_UPDATE`, `DATA_PRODUCT_VERSION_CREATION`) |
| `-ve, --verbose` | false                             | If `true`, the output of each validation result will be printed.                                                                                                                        |

