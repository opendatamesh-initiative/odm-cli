# odmcli init

## Usage

odm-cli init

## Description

This command initializes a new data product descriptor template with the given domain and name. The template can be customized using optional parameters. The initialized descriptor is stored in a JSON or YAML file.

## Options

| Option                 | Default                                          | Description                                                                                                                              |
|------------------------|--------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| --domain             |                                                  | Mandatory: The domain of the data product (e.g., finance, marketing).                                                                    |
| --name               |                                                  | Mandatory: The name of the data product (e.g., product-sales, customer-engagement).                                                      |
| --displayName        | Generated from --name                            | Optional: A human-readable name for the data product. Default is generated from --name.                                                  |
| --version            | 1.0.0                                            | Optional: The version of the data product. Default is 1.0.0.                                                                             |
| --fullyQualifiedName | Generated from --domain, --name, --version       | Optional: A unique identifier for the data product. Default is generated from --domain, --name, and --version.                           |
| --description        | null                                             | Optional: A brief description of the data product. Default is null.                                                                      |
| --ownerId            | Collected from the CLI configuration (if provided) | Optional: The ID of the owner of the data product. If not provided, this will be loaded from the CLI configurations.                     |
| --ownerName          | Collected from the CLI configuration (if provided) | Optional: The name of the owner of the data product. If not provided, this will be loaded from the CLI configurations.                   |
| --outputFile, -o   | ./data-product-descriptor.json                   | Optional: Specifies the path where the template will be stored, either a json or a yaml file. Default is ./data-product-descriptor.json. |
| --force              | false                                            | Optional: Forces the overwriting of the output file if it already exists.                                                                |

## Examples

### Initialize a Data Product Descriptor

bash
odmcli init \
  --domain finance \
  --name product-sales \
  --version 1.0.0 \
  --description "This template describes the product sales data." \
  --outputFile ./product-sales-template.json


### Initialize a Data Product Descriptor

bash
odmcli init \
  --domain marketing \
  --name customer-engagement \
  --displayName "Customer Engagement Data" \
  --version 1.2.3 \
  --fullyQualifiedName "marketing.customer-engagement.1.2.3" \
  --ownerId "owner123" \
  --ownerName "John Doe" \
  --outputFile ./customer-engagement-template.json


### Force Overwriting of the Output File

bash
odmcli init \
  --domain finance \
  --name product-sales \
  --outputFile ./product-sales-template.json \
  --force

