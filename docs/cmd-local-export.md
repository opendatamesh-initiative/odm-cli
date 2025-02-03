# odmcli local export

## Usage

`odmcli local export`

## Description

Export a source descriptor element (ex. output-port) to an external target (ex. jdbc). 

## Options

Option|Default|Description
-------|----------|-------
`-f, --file`|PATH/data-product-descriptor.json|Name of the descriptor file
`--from`| |Source type
`--source`| |Source name
`--to`| |Target type
`--target`| |Target name
`--target-system`| |Target system name, used to locate access properties stored in the `⁓/.odmcli/settings.json`
`--in-param <KEY=VALUE>`| |Parameters related to source
`--out-param <KEY=VALUE>`| |Parameter related to target

## Examples

### Export a descriptor to yaml
```bash
./odmcli local export -f dpdj/data-product-descriptor.json \
      --from descriptor \
      --to yaml --target dpdy/data-product-descriptor.yaml
```

### Export a descriptor to text
```bash
./odmcli local export -f dpd/data-product-descriptor.json \
      --from descriptor \
      --to text 
```
Because the target is not defined the export results is printed to stdout

### Export a descriptor to md
```bash
./odmcli local export -f dpd/data-product-descriptor.json \
      --from descriptor \
      --to md --target ./data-product-doc.md
```

### Export a descriptor to html
```bash
./odmcli local export -f dpd/data-product-descriptor.json \
      --from descriptor \
      --to html --target ./data-product-doc.html
```

### Export a descriptor as a DPROD ttl file
```bash
./odmcli local export -f dpd/data-product-descriptor.json \
      --from descriptor \
      --to dprod --target dprod.ttl
```

### Export an output port to a sql-ddl
```bash
./odmcli export -f dpd/data-product-descriptor.json \
      --from output-port --source oport123 \
      --to sql-ddl --target mysql.ddl 
```

### Export an output port to a kafka schema registry
```bash
./odmcli export -f dpd/data-product-descriptor.json \
      --from output-port --source oport123 \
      --to schema-registry --target subject123 --target-system kafka-registry 
```





