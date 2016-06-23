<#include "layout.ftl">

<#macro content>
<div class="panel panel-success">
    <div class="panel-heading">
        Configuration Edit<a href="/ui/" class="btn btn-link">Back</a>
    </div>
    <div class="panel-body">
        <form class="form-horizontal" id="add_form" method="POST">
          <div class="form-group">
            <label for="key" class="col-sm-2 control-label">Key</label>
            <div class="col-sm-10">
            	<#if kv?exists>
                <input type="text" name="key" value="${kv.key}" class="form-control" readonly>
                <#else>
                <input type="text" name="key" value="" class="form-control" required placeholder="key">
                </#if>
            </div>
          </div>
          <div class="form-group">
            <label for="value" class="col-sm-2 control-label">Value</label>
            <div class="col-sm-10">
            	<#if kv?exists>
                <textarea name="value" class="form-control" rows="10" required placeholder="json value">${kv.value?web_safe}</textarea>
                <#else>
                 <textarea name="value" class="form-control" rows="10" required placeholder="json value"></textarea>
                </#if>
            </div>
          </div>
          <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <a href="/ui/"class="btn btn-default">Cancel</a>
                <button type="submit" class="btn btn-success">Submit</button>
            </div>
          </div>
      </div>
</div>
</#macro>
<#macro scripts>
<script type="text/javascript">
function validateJson(el) {
    var params = $.trim(el.val());
    var ok = true;
    try {
        JSON.parse(params);
    }catch(e) {
        ok = false;
    }
    if (params.charAt(0) != '{') {
        ok = false;
    }
    if(ok) {
        el.parent().removeClass("has-error")
    } else {
        el.parent().addClass("has-error")
    }
    return ok;
}
$(function() {
	$("#add_form").submit(function() {
		return validateJson($("textarea[name=value]"));
	});
});
</script>
</#macro>
<@render />