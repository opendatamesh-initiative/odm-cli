# odmcli local get

## Usage

`odmcli local add`

## Description

Print on stdout an element from the descriptor

**⚒️Implementation details:**

1. TODO

## Options

Option|Default|Description
-------|----------|-------
`-f, --file`|PATH/data-product-descriptor.json|Name of the descriptor file
`--type`|equal to the value of `entityType` field in the `element` definition| Element type
`--element`||File that contains the element definition

## Examples

### Add the output-port defined in file new-oport.json
```bash
./odmcli local add -f dpd/data-product-descriptor.json \
     --element new-oport.json
```





