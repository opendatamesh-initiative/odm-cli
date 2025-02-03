# odmcli local import

## Usage

`odmcli local import`

## Description

Import from an external source (ex. jdbc) to a descriptor target element  (ex. output-port)

## Options

Option|Default|Description
-------|----------|-------
`-f, --file`|PATH/data-product-descriptor.json|Name of the descriptor file
`--from`| |Source type
`--source`| |Source objects to import
`--source-system`| |Source system name, used to locate access properties stored in the `⁓/.odmcli/settings.json`
`--to`| |Target type
`--target`| |Target name
`--in-param <KEY=VALUE>`| |Parameters related to source
`--out-param <KEY=VALUE>`| |Parameter related to target

## Examples

### Create a new output-port from a sql-ddl
```bash
./odmcli local import -f dpd/data-product-descriptor.json \
      --from sql-ddl --source test.sql \
      --to output-port --target oport123\
      --out-param database=odmdb
```

### Create a new output-port from a jdbc-table
```bash
./odmcli local import -f dpd/data-product-descriptor.json \
      --from jdbc-tables --source tableA,TableB --source-system crm
      --to output-port --target oport123\
```

### Create a new output-port from a template
```bash
./odmcli local import -f dpd/data-product-descriptor.json \
      --from template --source port-template.json
      --to output-port --target oport123\
      --out-param param1=value1 \
      --out-param param2=value2 \
      --out-param param3=value3 \
```



