<#include "layout.ftl">

<#macro styles>
<style>
.button-wrapper .btn {
    margin-bottom:10px;
}
</style>
</#macro>

<#macro content>
<#include "config_show.ftl">
<div class="panel panel-success">
	<div class="panel-heading">
		Name<a href="/ui/"> ${seq.name}</a>
		<#if config.readonly()>
		<#else>
		<a href="javascript:void(0)"
            class="btn btn-link btn-sm pull-right"
            data-toggle="popover"
            data-html="true"
            data-placement="left"
            data-content="<a class='btn btn-danger' href='/ui/seq/del?name=${seq.name}'>Delete Now</a>"><span class="glyphicon glyphicon-remove"></span></a>	
		</#if>
	</div>
	<div class="panel-body">
		<#list seq.ids as state>
		<#if state>
		<div class="btn-group button-wrapper">
		  <button type="button" class="btn btn-success">${state?counter - 1}</button>
		  <button type="button" class="btn btn-success dropdown-toggle" data-toggle="dropdown">
		    <span class="caret"></span>
		  </button>
		  <ul class="dropdown-menu">
		    <li><a href="/ui/seq/release?name=${seq.name}&id=${state?counter - 1}">Release Now</a></li>
		  </ul>
		</div>
		<#else>
		<div class="btn-group">
		  <button type="button" class="btn btn-primary">${state?counter - 1}</button>
		</div>
		</#if>
		</#list>
	</div>
</div>
</#macro>

<#macro scripts>
<script>
	$(function() {
		$('[data-toggle="popover"]').popover();
	});
</script>
</#macro>

<@render />