<?php
require_once "./views/preview.php";



main();
function main(){

	$aid = empty($_GET['aid']) ? 163 : intval( $_GET['aid']);

	$art_record = get_article_info( $aid );
	$html = preview_page_html( $art_record );
	echo $html;
}


function get_article_info( $aid ){

//	$art = array(
//		'imgurl' => 'http://www.hearheart.com/images/2015-02-288029b539f859513f2a4880381a6a5d4c.jpg',
//		'soundurl' => 'http://www.hearheart.com/sounds/2015-02-28ee175adfbe21597cca75705e527ef654.mp3',
//	);
	$raw_data = query_mongo_data( $aid );
	$art = $raw_data;
	
	$art['imgurl'] = "http://www.hearheart.com/images/" . $art['imgfile'];
	$art['soundurl'] = "http://www.hearheart.com/sounds/" . $art['soundfile'];

	$date_str = date("Y-m-d", $art['showtime']/1000);
	$art['date_str'] = $date_str;

	return $art;
}



function query_mongo_data( $aid ){
	$mongo_conf = array(
		'uri' => 'mongodb://localhost:27017',
		'db' => 'listendb',
		'password' => 'feeling0nolimit1',
	);

	$mongo_cli = new MongoClient( $mongo_conf['uri'],
					array( 	'password' => $mongo_conf['password'],
						'db' => $mongo_conf['db'],
					 ) 
			);

	$db = $mongo_cli->selectDB("listendb");
	$col = $db->col_page;
	$cursor = $col->find( array( "pageno" => $aid ) );
	$data_record = $cursor->getNext();

	return $data_record;
}

