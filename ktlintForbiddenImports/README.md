# Custom ktlint rules

### When modifying this code

Whenever KTLINT_DEBUG env variable is set to "ast" or -DktlintDebug=ast is used
com.pinterest.ktlint.test.(lint|format) will print AST (along with other debug info) to the stderr.
This can be extremely helpful while *writing* and **testing** rules.
```
System.setProperty("ktlintDebug", "ast")
```

### Based on
https://github.com/pinterest/ktlint/blob/master/ktlint-ruleset-template