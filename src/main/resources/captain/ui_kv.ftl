<#include "layout.ftl">

<#macro styles>
<link href="/css/jquery.jsonview.css" rel="stylesheet">
</#macro>

<#macro content>
<#include "config_show.ftl">
<div class="panel panel-success">
	<div class="panel-heading">
		Key<a href="/ui/"> ${kv.key}</a> [version=${kv.version}]
		<#if config.readonly()>
		<#else>
		<a class="btn btn-link" href="/ui/kv/edit?key=${kv.key}">Edit</a>
		<a href="javascript:void(0)"
            class="btn btn-link btn-sm pull-right"
            data-toggle="popover"
            data-html="true"
            data-placement="left"
            data-content="<a class='btn btn-danger' href='/ui/kv/del?key=${kv.key}'>Delete Now</a>"><span class="glyphicon glyphicon-remove"></span></a>	
		</#if>
	</div>
	<div class="panel-body">
		<div id="kv-json">
		</div>
	</div>
</div>
</#macro>

<#macro scripts>
<script type="text/javascript" src="/js/jquery.jsonview.min.js"></script>
<script>
	var json = ${kv.value};
	$(function() {
		$('[data-toggle="popover"]').popover();
		$("#kv-json").JSONView(json);
	});
</script>
</#macro>

<@render />