# odmcli local get

## Usage

`odmcli local get`

## Description

Print on stdout an element from the descriptor

**⚒️Implementation details:**

1. TODO

## Options

Option|Default|Description
-------|----------|-------
`-f, --file`|PATH/data-product-descriptor.json|Name of the descriptor file
`--element`||Type of the element to get (es. info, input-port, app, infra)
`--name`||Name of the element to get

## Examples

### Get the output-port oport123
```bash
./odmcli local get -f dpd/data-product-descriptor.json \
     --element output-port --name oport123
```





