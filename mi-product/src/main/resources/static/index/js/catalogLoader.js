$(function(){
    $.getJSON("index/catalogjson/catalog",function (data) {

        var ctgall=data;
        $(".header_main_left_a").each(function(){
            var ctgnums= $(this).attr("ctg-data");
            if(ctgnums){
                var panel=$("<div class='header_main_left_main'></div>");
                var panelol=$("<ol class='header_ol'></ol>");
                var  ctgnumArray = ctgnums.split(",");
                $.each(ctgnumArray,function (i,ctg1Id) {
                    var ctg2list= ctgall[ctg1Id];
                    $.each(ctg2list,function (i,ctg2) {
                        var cata2link=$("<a href='#' style= 'color: #111; font-weight: 700;' class='aaa'>"+ctg2.name+"  ></a>");


                        console.log(cata2link.html());
                        var li=$("<li></li>");
                        var  ctg3List=ctg2["catalog3List"];
                        var len=0;
                        $.each(ctg3List,function (i,ctg3) {
                            var cata3link = $("<a onclick='catelog3(this)' class='catelog3' href=\"http://search.dlqk8s.top:81/list.html?catalog3Id="+ctg3.id+"\" style=\"color: #999;\">" + ctg3.name + "</a>");
                            li.append(cata3link);
                            len=len+1+ctg3.name.length;
                        });
                        if(len>56&&len<110){
                            li.attr("style","height: 54px;");
                        }else if(len>=110){
                            li.attr("style","height: 77px;");
                        }
                        panelol.append(cata2link).append(li);

                    });

                });
                panel.append(panelol);
                $(this).after(panel);
                $(this).parent().addClass("header_li2");
            }
        });
    });
});
