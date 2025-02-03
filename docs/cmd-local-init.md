# odmcli local init

## Usage

`odmcli local init`

## Description

Initializes a descriptor file. 

**⚒️Implementation details:**

1. The command MUST create the folders in the specified `PATH` if they do not exist yet
2. The command MUST give an error and stop if the descriptor file already exists
3. The command creates a descriptor file using the `⁓/.odmcli/templates/init-template.json` and replacing its variables with the value received as `init-param` or contained in the `.odmenv` file
4. The command MUST give an error if some variables are not resolved with corresponding parameters. The unmatched variables MUST be printed out
5. The command generates the following subfolders  `PATH/ports`,  `PATH/apps`, and  `PATH/infra`
6. The command MUST create a dedicated folder for each interface element defined in the descriptor in this form `PATH/ports/interfaceComponentName`
7. The command MUST create a dedicated folder for each application element defined in the descriptor in this form `PATH/apps/applicationComponentName`
8. The command MUST create a dedicated folder for each infrastructural element defined in the descriptor in this form `PATH/apps/infrastructuralComponentName`
9. The command MUST save interface, application, and infrastructural definitions as separated files stored in the associate subfolders. The descriptor's root entity then points to these definitions using the `$ref` field. 
10. The command MUST delete all folders and files created if an error occurs


## Options

Option|Default|Description
-------|----------|-------
`-f, --file`|`PATH/data-product-descriptor.json`|Name of the descriptor file
`--init-param <KEY=VALUE>`| |Initialization parameters

## Examples

### Create a new descriptor file
```bash
./odmcli local init -f dpd/data-product-descriptor.json \
     --init-param product-name=product123 \
     --init-param info.owner.id=john.doe@newco.com
```



