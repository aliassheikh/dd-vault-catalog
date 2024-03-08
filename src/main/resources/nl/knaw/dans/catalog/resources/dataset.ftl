<!DOCTYPE html>
<html lang="en">
<head>
    <title>DANS Data Vault Catalog - ${dataset.nbn} - ${dataset.title!"n/a"}</title>
    <link rel="stylesheet" type="text/css" href="/assets/css/styles.css">
</head>
<body>
<h1>DANS Data Vault Catalog</h1>
<h2>${dataset.title!"n/a"}</h2>

<table class="dataset-metadata">
    <tr>
        <td class="field-name">NBN</td>
        <td class="field-value">${dataset.nbn!"n/a"}</td>
    </tr>
    <tr>
        <td class="field-name">Data Station</td>
        <td class="field-value">${dataset.datastation!"n/a"}</td>
    </tr>
    <#if dataset.dataversePid??>
        <tr>
            <td class="field-name">Dataverse PID</td>
            <td class="field-value">${dataset.dataversePid}</td>
        </tr>
    </#if>
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

<#list dataset.datasetVersionExports as dve>
    <h3>v${dve.ocflObjectVersionNumber!"n/a"}</h3>
    <#if dve.deaccessioned??>
        <p class="deaccessioned">This version has been deaccessioned</p>
    <#else>
        <table class="dve-metadata">
            <tr>
                <td class="field-name">Bag ID</td>
                <td class="field-value">${dve.bagId!"n/a"}</td>
            </tr>
            <tr>
                <td class="field-name">OCFL Object version</td>
                <td class="field-value">${dve.ocflObjectVersionNumber!"n/a"}</td>
            </tr>
            <tr>
                <td class="field-name">Creation timestamp</td>
                <td class="field-value">${dve.createdTimestamp!"n/a"}</td>
            </tr>
            <tr>
                <td class="field-name">Archival timestamp</td>
                <td class="field-value">${dve.archivedTimestamp!"Not archived yet"}</td>
            </tr>
            <#if dve.dataversePidVersion??>
                <tr>
                    <td class="field-name">DV PID version</td>
                    <td class="field-value">${dve.dataversePidVersion}</td>
                </tr>
            </#if>
            <#if dve.otherId??>
                <tr>
                    <td class="field-name">Other ID</td>
                    <td class="field-value">${dve.otherId}</td>
                </tr>
            </#if>
            <#if dve.otherIdVersion??>
                <tr>
                    <td class="field-name">Other ID version</td>
                    <td class="field-value">${dve.otherIdVersion}</td>
                </tr>
            </#if>
            <#if dve.exporter??>
                <tr>
                    <td class="field-name">Exporter</td>
                    <td class="field-value">${dve.exporter}</td>
                </tr>
            </#if>
            <#if dve.exporterVersion??>
                <tr>
                    <td class="field-name">Other ID version</td>
                    <td class="field-value">${dve.exporterVersion}</td>
                </tr>
            </#if>
        </table>
    </#if>
</#list>

</body>
</html>