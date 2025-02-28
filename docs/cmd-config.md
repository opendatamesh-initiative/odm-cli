# odm-cli local

## Usage

`odm-cli config [OPTIONS] [PARAMETERS]`

## Description

Handles the configuration file located at `$HOME/.odmcli/application.yml`, which overrides the default CLI settings.

## Options

| Command             | Description                                                                                                                                                                                                                |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `--print, -p`       | Prints the current configuration.                                                                                                                                                                                          |
| `--set, -s`         | Adds a property in the configuration file. E.g. `--set cli.env.property=propretyValue`.                                                                                                                                    |
| `--unset, -u`       | Deletes a property of the configuration file. E.g. `--unset cli.env.property`.                                                                                                                                             |
| `--setarray, -sa`   | If not present, adds the specified array property  in the configuration file. Then, if specified, adds an element to the array with the given properties and values. E.g. `--setarray cli.env.arrayproperty  field=value`. |
| `--unsetarray, -ua` | Removes elements of array fields in the configuration file that matches the given properties. E.g. `--unsetarray cli.env.arrayproperty  field=value`.                                                                      |
