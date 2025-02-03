# odmcli local init

## Usage

`odmcli local lint [OPTION] [FILE]`

FILE is the output descriptor file. If not defined the input descriptor file is overwritten

## Description

Lint a descriptor file. 

**⚒️Implementation details:**

1. The command MUST take the descriptor to the canonical structure with elements split in separated folders on the base of their types ( `PATH/ports`,  `PATH/apps`, and  `PATH/infra`)


## Options

Option|Default|Description
-------|----------|-------
`-f, --file`|PATH/data-product-descriptor.json|Name of the descriptor file

## Examples

### Lint an old descriptor file
```bash
./odmcli local init -f dpd/data-product-descriptor.json lint/data-product-descriptor.json
```



