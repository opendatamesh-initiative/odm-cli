# odmcli local rm

## Usage

`odmcli local rm`

## Description

Remove an element from the descriptor

**⚒️Implementation details:**

1. TODO

## Options

Option|Default|Description
-------|----------|-------
`-f, --file`|PATH/data-product-descriptor.json|Name of the descriptor file
`--type`||Element type
`--name`||Element name. If not defined remove all elements of the given `type`
`--recursive`|`false`|`true` to remove also referenced elements recursively
`--force`|`false`|`true` to ask to confirm before to remove


## Examples

### Remove the output-port oport123
```bash
./odmcli local rm -f dpd/data-product-descriptor.json \
     --type output-port --name oport123
```

### Remove all output-ports and related elements without asking for a confirmation
```bash
./odmcli local rm -f dpd/data-product-descriptor.json \
     --type output-port --recursive  --force
```





