<?php
function preview_page_html( $data ){
	$page_content =<<<EOF
<html>
    <head>
        <meta charset="UTF-8" />
        <title>
            听见
        </title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="bootstrap.min.css" rel="stylesheet">
        
        <style>
            html, body
            {
                background-image: url(img/article_bg.png);
                background-size: 100% 100%;
                margin: 0;
                color: #ffffff;
            }
            .main_wrapper
            {
                position: absolute;
                width: 100%;
                height: 100%;
            }
            .top_navi
            {
                background-color: #151515;
                padding-top: 0.8em ;
                padding-bottom: 0.8em;
                text-align: center;
                
            }
            .main
            {
                overflow: hidden;
            }
            .article_block
            {
                margin-top: 5%;
                margin-left: 14px;
                margin-right: 14px;
                margin-bottom: 10%;
                
            }
            .article_img_block
            {
                background-image: url("{$data['imgurl']}");
                background-size: 100% 100%;

                width: 100%;
                display: inline-block; 
                text-align: center;
                vertical-align: middle;
                
                padding-top: 35%;
                
            }
            .article_img_block img
            {
                vertical-align: middle;
                width: 15%;
            }
            .play_icon
            {
                padding-top: 40%;
                height: 20%;
                width: 20%;
                background: #000000;
                
            }
            .cover_title
            {
                margin-left: -70%;
                margin-top: 30%;
                margin-bottom: 5%;
                
            }
            .article_info
            {
                padding-top: 13px;
                padding-left: 13px;
                padding-right: 13px;
                padding-bottom: 13px;
                background-color: #140E0B;
                opacity: 0.65;
            }
            img{
                -webkit-tap-highlight-color:rgba(0,0,0,0);

            }
            .article_info .author
            {
                text-align: right;
            }
            
            .footage
            {
                position: absolute;
                background-color: #130D0A;
                width:100%;
                top: 87%;
                height: 13%;
            }
            .footage_content
            {
                position: relative;
                margin-top: auto;
                margin-bottom: auto;
                margin-left: 4%;
                padding-top: 15px;
            }
            .footage_content img
            {
                margin-left: 0px;
                width: 45px;
            }
            
            .footage_text
            {
                padding-left: 15%;
            }
            .footage_content .download
            {
                float: right;
                background-color: #31A517;
                color: #FFFFFF;
                font: bold 12px/25px;
                width: 32%;
                height: 45px;
                line-height: 45px;
                
                margin-right: 8%;
      
                font: bold 1.5em;
                text-align:center;
                vertical-align: middle;
                font-size: 130%;
                
                border-radius: 6px;
            }
        </style>
    </head>
    
    <body>
        <div class="main_wrapper">
            <div class="main">
                <div class="article_block">
                    <div class="article_img_block" >

                        <img id="play_status_icon" onclick="change_play_status();return false;" src="img/play.png" alt="play_status" />

                        <div class="cover_title">
                            <font>VOL.{$data["pageno"]}</font>
                            <br/>
                            <font>{$data['date_str']}</font>
                        </div>
                    </div>
                    <div class="article_info">
                        <div>
                        <font>{$data["txt"]}</font>
                        </div>
                        <br/>
                        <div class="author">
                            <font>{$data["author"]}</font>
                        </div>
                    </div>
                </div>
            </div>
            
            <div style="display:" class="footage">
                <div class="footage_content">
                    <img style="float:left;" src="img/ic_launcher.png" alt="hear" />
                    
                    <div class="footage_text">
                        <span class="download" href="#">下载</span>
                        <b>听见</b><br>静宁时刻，安静聆听
                       
                    </div>
                </div>
            </div>
        </div>
        <audio id="audio_player" src="{$data['soundurl']}" style="display:inline"  preload="auto">
        </audio>

        
    </body>
    <script>
    act_onload();
    function act_onload(){
        console.log("start debug");
    }

    function change_play_status(){
        var audio_elem = document.getElementById("audio_player");
        var play_icon = document.getElementById("play_status_icon");

        if( audio_elem.paused ){
            audio_elem.play();
            play_icon.src = "img/pause.png";
        }else{
            audio_elem.pause();
            play_icon.src = "img/play.png";
        }
    }


    </script>
</html>
EOF;


echo $page_content;
}
