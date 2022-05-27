package my.miraiplus.annotation

import org.intellij.lang.annotations.Language

annotation class MessageHandle(@Language("RegExp") val pattern: String, vararg val option: RegexOption = [])
