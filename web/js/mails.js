/**
 * Created by pov on 19/03/17.
 */

MAILTEMPLATE =
    "<li role='presentation'><a href='#' onclick='display(\"#MAILID#\");'>" +
    "<span class='subject'>#SUBJECT#</span>" +
    "<span class='from'>#MAILFROM#</span>" +
    "<span class='date'>#DATE#</span>" +
    "</a></li>";

$(document).ready(function () {
    // recup de la liste des mails
    $.getJSON("search", function (data) {
        i = 0;
        data.forEach(function (element) {
            mail = MAILTEMPLATE
                .replace(new RegExp('#MAILID#', 'g'), element.mailid)
                .replace('#MAILFROM#', element.mailfrom)
                .replace('#DATE#', element.dt)
                .replace('#SUBJECT#', element.subject);

            $("#mailtable").append(mail);
            i++;
        });
    });

    var nextPage = 2;

    // Each time the user scrolls
    $("#mailtable").scroll(function () {
        // End of the document reached?
        var scrollHeight = $("#mailtable").prop('scrollHeight');
        var divHeight = $("#mailtable").height();
        var scrollerEndPoint = scrollHeight - divHeight - 10;

        var divScrollerTop = $("#mailtable").scrollTop();
        if (divScrollerTop >= scrollerEndPoint) {
            $.getJSON("search?next=" + nextPage, function (data) {
                data.forEach(function (element) {
                    mail = MAILTEMPLATE
                        .replace(new RegExp('#MAILID#', 'g'), element.mailid)
                        .replace('#MAILFROM#', element.mailfrom)
                        .replace('#DATE#', element.dt)
                        .replace('#SUBJECT#', element.subject);

                    $("#mailtable").append(mail);
                });
            });
            nextPage++;
        }
    });
});

var DISPLAY_MAIL_HEADER = "<div class='mailheader'><span class='from'>From: ##FROM##</span><span class='to'>To: ##TO##</span><span class='cc'>Cc: ##CC##</span><span class='bcc'>Bcc: ##BCC##</span><span class='date'>Date: ##DATE##</span><span class='subject'>Subject: ##SUBJECT##</span><span class='attachments'>Attachments: ##ATTACHMENTS##</span> </div>";

function display(mailid) {
    $.getJSON("display", {mailid: mailid}).done(function (data) {
        data.forEach(function (element) {
            var mailbody = element.body;
            if (element.bodytype !== 1)
                mailbody = "<pre>" + element.body + "</pre>";

            // build mail header
            var to = [];
            var cc = [];
            var bcc = [];
            for (var i = 0; i < element.recipients.length; i++) {
                if (element.recipients[i].recipienttype === "TO")
                    to[to.length] = element.recipients[i].address.replace("<", "&lt;").replace(">", "&gt;");
                else if (element.recipients[i].recipienttype === "CC")
                    cc[cc.length] = element.recipients[i].address.replace("<", "&lt;").replace(">", "&gt;");
                else if (element.recipients[i].recipienttype === "BCC")
                    bcc[bcc.length] = element.recipients[i].address.replace("<", "&lt;").replace(">", "&gt;");
            }

            var attachments = [];
            for (var i = 0; i < element.attachments.length; i++) {
                attachments[attachments.length] = element.attachments[i].filename;
            }

            var content = DISPLAY_MAIL_HEADER.replace("##FROM##", element.mailfrom.replace("<", "&lt;").replace(">", "&gt;"))
                    .replace("##SUBJECT##", element.subject)
                    .replace("##DATE##", element.dt)
                    .replace("##TO##", to.join(', '))
                    .replace("##CC##", cc.join(', '))
                    .replace("##BCC##", bcc.join(', '))
                    .replace("##ATTACHMENTS##", attachments.join(', '))
                + mailbody;

            $("#mailcontent").html(content);
            $("#mailcontent").scrollTop(0);
        });
    });
}