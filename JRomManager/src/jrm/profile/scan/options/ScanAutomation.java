/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.scan.options;

import jrm.locale.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// TODO: Auto-generated Javadoc
/**
 * The Enum ScanAutomation.
 *
 * @author opty
 */
public 
 /**
  * Instantiates a new scan automation.
  *
  * @param desc the desc
  */
 @RequiredArgsConstructor enum ScanAutomation
{
	
	/** The scan. */
	SCAN(Messages.getString("ScanAutomation.Scan")), //$NON-NLS-1$
	
	/** The scan report. */
	SCAN_REPORT(Messages.getString("ScanAutomation.ScanReport")), //$NON-NLS-1$
	
	/** The scan report fix. */
	SCAN_REPORT_FIX(Messages.getString("ScanAutomation.ScanReportFix")), //$NON-NLS-1$
	
	/** The scan report fix scan. */
	SCAN_REPORT_FIX_SCAN(Messages.getString("ScanAutomation.ScanReportFixScan")), //$NON-NLS-1$
	
	/** The scan report fix scan. */
	SCAN_FIX(Messages.getString("ScanAutomation.ScanFix")); //$NON-NLS-1$
	
	/** The desc. */
	private final 
	/**
	 * Gets the desc.
	 *
	 * @return the desc
	 */
	@Getter String desc;
	
	public boolean hasReport()
	{
		return this==SCAN_REPORT || this==SCAN_REPORT_FIX || this==SCAN_REPORT_FIX_SCAN;
	}
	
	public boolean hasFix()
	{
		return this==SCAN_FIX || this==SCAN_REPORT_FIX || this==SCAN_REPORT_FIX_SCAN;
	}
	
	public boolean hasScanAgain()
	{
		return this==SCAN_REPORT_FIX_SCAN;
	}
}
