---
description: "Use when writing, editing, or generating Javadoc in Java source files — class, method, field, and package-level documentation. Covers Javadoc tag usage, Lombok @Getter/@Setter/@Data field documentation rules, HTML-safe formatting, and English-only comments."
applyTo: "**/*.java"
---

# Javadoc Conventions for JRomManager

These conventions apply to **all Java source edits** in JRomManager. For one-off, end-to-end Javadoc generation tasks (documenting a whole file or set of files), invoke the `/javadoc` prompt.

## Scope of documentation
- Document at the **class**, **method**, and **field** levels — including private, package-private, and protected members.
- Existing well-formed, accurate **English** Javadoc is left untouched; only fix Javadoc that is incomplete, inaccurate, or not in English.
- Translate any non-English comments or Javadoc into professional English.

## Format
- Javadoc is written directly in the Java source, never as Markdown-formatted comments.
- Files are UTF-8 without BOM.
- The first sentence is a concise summary ending with a period, followed by detailed paragraphs as needed.
- Keep Javadoc HTML-safe where elements are used (e.g. `<p>`, `<ul>`, `<li>`, `<pre>`).
- Standard tags: `@param`, `@return`, `@throws`, `@see`, `@since`, `{@link ...}`, `{@code ...}`.
- `@since` and `@author` are optional — include only when a meaningful value is available. Existing package docs use `@author optyfr`.

## Lombok rules
- Declare `@param` in a field's Javadoc if `@Setter` is present on the field, or `@Data` on its class.
- Declare `@return` in a field's Javadoc if `@Getter` is present on the field, or `@Data` on its class.
- Delombok distributes these across generated accessors — do **not** mention Lombok in the Javadoc text.

## Editing constraints
- Within a target Java file, you may edit Javadoc/comment text and add imports strictly needed for `{@link}` references. Do not alter code logic.

## Example

```java
/**
 * Wraps a standard JDK {@link java.security.MessageDigest} to compute MD5 or SHA-1 hashes.
 *
 * @since 2.5
 */
public class MsgDigest extends MDigest
{
    /**
     * The user display name.
     * @param name the user display name
     */
    @Setter
    private String name;

    /**
     * The user roles.
     * @return the user roles
     */
    @Getter
    private String[] roles;
}
```
