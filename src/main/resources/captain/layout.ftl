<#macro content>
</#macro>

<#macro styles>
</#macro>

<#macro scripts>
</#macro>

<#macro render>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta http-equiv="cache-control" content="no-cache">
    <title>Captain Dashboard</title>
    <link href="/css/bootstrap.min.css" rel="stylesheet">
    <@styles />
  </head>
  <body>
  	<@content />
  </body>
  <script type="text/javascript" src="/js/jquery.min.js"></script>
  <script type="text/javascript" src="/js/bootstrap.min.js"></script>
  <@scripts />
</html>
</#macro>