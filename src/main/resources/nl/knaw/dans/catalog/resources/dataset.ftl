<!DOCTYPE html>
<html lang="en">
<head>
    <title>DANS Data Vault Catalog - ${dataset.nbn} - ${dataset.title!"No title"}</title>
    <link rel="stylesheet" type="text/css" href="/assets/css/styles.css">
</head>
<body>
<h1>DANS Data Vault Catalog</h1>
<h2>${dataset.title!"No title"}</h2>

<table>
    <tr>
        <td class="field-name">urn:nbn</td>
        <td class="field-value">${dataset.nbn!"n/a"}</td>
    </tr>
    <#if dataset.dataSupplier??>
    <tr>
        <td class="field-name">Data supplier</td>
        <td class="field-value">${dataset.dataSupplier}</td>
    </tr>
    </#if>
    <#if dataset.swordToken??>
    <tr>
        <td class="field-name">SWORD token</td>
        <td class="field-value">${dataset.swordToken!"n/a"}</td>
    </tr>
    </#if>
</table>


</body>
</html>