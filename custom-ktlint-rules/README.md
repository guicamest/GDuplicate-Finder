# Custom ktlint rules

## Forbidden imports rule
The suggested way of working with `files` starting with Java 7 is by using the NIO2 API.
The APIs that should be used are mostly `Path(s)` and `Files.`.
In order to guarantee that there are no imports of the `old` (_java.io.File_) API in the
library (_core_) project, a custom ktlint rule is used and applied to the library project.

## How to use

Among the dependencies:
```
dependencies {
  ...
  ktlintRuleset project(":custom-ktlint-rules")
  ...
}
```

## When modifying this code

Whenever KTLINT_DEBUG env variable is set to "ast" or -DktlintDebug=ast is used
com.pinterest.ktlint.test.(lint|format) will print AST (along with other debug info) to the stderr.
This can be extremely helpful while *writing* and **testing** rules.
```
System.setProperty("ktlintDebug", "ast")
```

## Based on
* https://github.com/pinterest/ktlint/blob/master/ktlint-ruleset-template
* https://medium.com/@vanniktech/writing-your-first-ktlint-rule-5a1707f4ca5b