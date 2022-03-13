module jrmcli
{
	exports jrm.cli;

	requires jrmcore;
	requires static lombok;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires jcommander;
	requires minimal.json;
	opens jrm.cli to jcommander;
}
