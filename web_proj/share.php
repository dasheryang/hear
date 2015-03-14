<?php
require_once "./views/preview.php";



main();
function main(){
	$art_record = get_article_info( 10 );
	$html = preview_page_html( $art_record );
	echo $html;
}


function get_article_info( $aid ){
	$art = array(
		'imgurl' => 'http://www.hearheart.com/images/2015-02-288029b539f859513f2a4880381a6a5d4c.jpg',
		'soundurl' => 'http://www.hearheart.com/sounds/2015-02-28ee175adfbe21597cca75705e527ef654.mp3',
		 );
	return $art;
}

