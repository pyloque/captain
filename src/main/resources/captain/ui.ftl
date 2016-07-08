<#include "layout.ftl">

<#macro content>
<#include "config_show.ftl">
<#if stacktraces?exists>
<div class="panel panel-danger">
	<div class="panel-heading">Error StackTraces</div>
	<div class="panel-body">
		<ul class="list-group">
			<li class="list-group-item text-danger">${reason}</li>
			<#list stacktraces as trace>
			<li class="list-group-item text-danger">${trace}</li>
			</#list>
		</ul>
	</div>
</div>
<#else>
<div class="panel panel-success">
	<div class="panel-heading">All Services[version=${sversion?c}]</div>
	<div class="panel-body">
		<ul class="list-group">
			<#list services?keys as name>
			<li class="list-group-item"><span class="badge">${services[name]}</span><a href="/ui/service/?name=${name}">${name}</a></li>
			</#list>
		</ul>
	</div>
</div>
<div class="panel panel-success">
	<div class="panel-heading">
		All KeyValues[version=${kversion?c}]
		<#if config.readonly()>
		<#else>
		<a href="/ui/kv/edit" class="btn btn-link">Add</a>
		</#if>
	</div>
	<div class="panel-body">
		<ul class="list-group">
			<#list kvs as key>
			<li class="list-group-item"><a href="/ui/kv/?key=${key}">${key}</a></li>
			</#list>
		</ul>
	</div>
</div>
</#if>
</#macro>

<@render />