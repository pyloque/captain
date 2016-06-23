<#include "layout.ftl">

<#macro content>
<#include "config_show.ftl">
<div class="panel panel-success">
	<div class="panel-heading">Service<a href="/ui/"> ${name}</a> [version=${version}]</div>
	<div class="panel-body">
		<ul class="list-group">
			<#list services as service>
			<li class="list-group-item">${service.key} <label class="label label-success">${service.payload}</label><span class="badge">${service.ttl}</span></li>
			</#list>
		</ul>
	</div>
</div>
</#macro>

<@render />