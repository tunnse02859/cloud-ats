@(t : String)
@import controllers.test._

$(document).ready(function() {
      var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
      
      //Fetch VM Initializing Log
      var vmLog = new WS("@controllers.vm.routes.VMController.vmLog(TestController.getCompany().getId(), session().get("uuid")).webSocketURL(request)");
  
      vmLog.onmessage = function(event) {
        var data = JSON.parse(event.data);
        $(data.vms).each(function() {
          var id = this.id;
          var msg = this.msg;
          var pre = $("pre.pre-scrollable.vm-log-" + id);
          $(pre).append(msg);
        });
      };
      
      //Log
      var projectLog = new WS("@routes.TestController.projectLog(t, session().get("uuid"), session().get("user_id")).webSocketURL(request)");
      
      projectLog.onmessage = function(event) {
        var data = JSON.parse(event.data);
        $(data.logs).each(function() {
          var id = this.id;
          var msg = this.msg;
          var pre = $("div#log-" + id + " pre");
          
          if (msg.substring(0, 7) == 'vm-log-') {
            $(pre).addClass(msg);
            $(pre).append(".");
          } else {
            $(pre).append(msg);          
          }
        });
      }
      
      //Status
      var projectStatus = new WS("@routes.TestController.projectStatus(t, session().get("uuid"), session().get("user_id")).webSocketURL(request)");
      
      projectStatus.onmessage = function(event) {
        var data = JSON.parse(event.data);
        
        $(data.projects).each(function() {
          var projectId = this.id;
          $("tr.project-" + projectId + " td span.last-build").text(this.last_build);
          var status = $("tr.project-" + projectId + " td span.status");
          
          var projectRun = $("tr.project-" + projectId + " a.btn.run");
          var projectStop = $("tr.project-" + projectId + " a.btn.stop");
          var projectReport = $("tr.project-" + projectId + " a.btn.report");
          
          var color;
          switch (this.status) {
          case "Completed":
            $(projectReport).removeClass("disabled");
          case "Ready":
            color = "green";
            $(projectRun).removeClass("disabled");
            $(projectStop).addClass("disabled");
            break;
          case "Failure":  
            color = "";
            $(projectRun).removeClass("disabled");
            $(projectStop).addClass("disabled");
            $(projectReport).removeClass("disabled");
            break;
          case "Running":
            color = "cyan";
            $(projectRun).addClass("disabled");
            $(projectStop).removeClass("disabled");
            $(projectReport).addClass("disabled");
            break;
          case "Aborted":
            color = "orange";
            $(projectRun).removeClass("disabled");
            $(projectStop).addClass("disabled");
            $(projectReport).addClass("disabled");
            break;
          case "Errors":
            color = "red";
            $(projectRun).removeClass("disabled");
            $(projectStop).addClass("disabled");
            $(projectReport).removeClass("disabled");
            break;
          case "Initializing":
            color = "blue";
            $(projectRun).addClass("disabled");
            $(projectStop).removeClass("disabled");
            $(projectReport).addClass("disabled");
            break;
          default:
            color = "";
            break;
          }
          
          $(status).text(this.status);
          $(status).removeAttr('class');
          $(status).attr('class', 'badge status badge-' + color);
          
          $(this.snapshots).each(function(){
            var snapshotId = this.id;
            $("tr.project-snapshot-" + snapshotId + " td span.last-build").text(this.last_build);
            
            var status = $("tr.project-snapshot-" + snapshotId + " td span.status");
            var snapshotRun = $("tr.project-snapshot-" + snapshotId + " a.btn.run");
            var snapshotStop = $("tr.project-snapshot-" + snapshotId + " a.btn.stop");
            var snapshotReport = $("tr.project-snapshot-" + snapshotId + " a.btn.report");
            
            var color;
            switch (this.status) {
            case "Completed":
              $(snapshotReport).removeClass("disabled");
            case "Ready":
              color = "green";
              $(snapshotRun).removeClass("disabled");
              $(snapshotStop).addClass("disabled");
              break;
            case "Failure":  
              color = "";
              $(snapshotRun).removeClass("disabled");
              $(snapshotStop).addClass("disabled");
              $(snapshotReport).removeClass("disabled");
              break; 
            case "Running":
              color = "cyan";
              $(snapshotRun).addClass("disabled");
              $(snapshotStop).removeClass("disabled");
              $(snapshotReport).addClass("disabled");
              break;
            case "Aborted":
              color = "orange";
              $(snapshotRun).addClass("disabled");
              $(snapshotStop).removeClass("disabled");
              $(snapshotReport).addClass("disabled");
              break;
            case "Errors":
              color = "red";
              $(snapshotRun).removeClass("disabled");
              $(snapshotStop).addClass("disabled");
              $(snapshotReport).addClass("disabled");
              break;
            case "Initializing":
              color = "blue";
              $(snapshotRun).addClass("disabled");
              $(snapshotStop).removeClass("disabled");
              $(snapshotReport).addClass("disabled");
              break;
            default:
              color = "";
              break;
            }
            
            $(status).text(this.status);
            $(status).removeAttr('class');
            $(status).attr('class', 'badge status badge-' + color);
          });
        });
      };
      
      window.onbeforeunload = function() {
        projectStatus.onclose = function() {};
        projectStatus.close();
        projectLog.onclose = function() {};
        projectLog.close();
        vmLog.onclose = function() {};
        vmLog.close();
      };
    });