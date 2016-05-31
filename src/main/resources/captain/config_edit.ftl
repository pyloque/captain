<#include "layout.ftl">

<#macro content>
<div class="panel panel-success">
    <div class="panel-heading">
        Configuration Edit<a href="/ui/" class="btn btn-link">Back</a>
    </div>
    <div class="panel-body">
        <form class="form-horizontal" id="config_form" method="POST">
          <div class="form-group">
            <label class="col-sm-2 control-label">Bind Addr</label>
            <div class="col-sm-10">
                <input type="text" readonly value="${config.bindHost()}:${config.bindPort()?c}" class="form-control">
            </div>
          </div>
          <div class="form-group">
            <label for="redisHost" class="col-sm-2 control-label">Redis Host</label>
            <div class="col-sm-10">
                <input type="text" name="redisHost" value="${ config.redisHost() }" class="form-control" required placeholder="Redis Host">
            </div>
          </div>
          <div class="form-group">
            <label for="redisPort" class="col-sm-2 control-label">Redis Port</label>
            <div class="col-sm-10">
                <input type="number" min=1 max=65535 name="redisPort" value="${ config.redisPort()?c }" class="form-control" required placeholder="Redis Port">
            </div>
          </div>
          <div class="form-group">
            <label for="redisDb" class="col-sm-2 control-label">Redis Db</label>
            <div class="col-sm-10">
                <input type="number" min=0 max=100 name="redisDb" value="${ config.redisDb()?c }" class="form-control" required placeholder="Redis Db">
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label">Watch Interval</label>
            <div class="col-sm-10">
                <input type="number" min=100 max=5000 readonly value="${ config.interval()?c }" class="form-control" required placeholder="Watch Interval">
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

<@render />