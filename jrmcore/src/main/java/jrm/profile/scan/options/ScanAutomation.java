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

/**
 * Enumeration specifying different scan automation pipeline levels.
 * These levels control the automated flow of operations, including scanning folders,
 * generating a report, applying recommended repairs, and re-scanning to ensure consistency.
 * 
 * @author optyfr
 * @since 1.0
 */
@RequiredArgsConstructor
public enum ScanAutomation implements Descriptor
{
	/**
	 * Perform file and directory scanning only.
	 */
	SCAN(Messages.getString("ScanAutomation.Scan")), //$NON-NLS-1$
	
	/**
	 * Perform directory scanning and generate an active audit report.
	 */
	SCAN_REPORT(Messages.getString("ScanAutomation.ScanReport")), //$NON-NLS-1$
	
	/**
	 * Perform directory scanning, generate a report, and immediately attempt to fix any errors found.
	 */
	SCAN_REPORT_FIX(Messages.getString("ScanAutomation.ScanReportFix")), //$NON-NLS-1$
	
	/**
	 * Perform directory scanning, generate a report, apply repairs, and then re-scan the destination.
	 */
	SCAN_REPORT_FIX_SCAN(Messages.getString("ScanAutomation.ScanReportFixScan")), //$NON-NLS-1$
	
	/**
	 * Perform directory scanning and apply repairs directly without saving a persistent report.
	 */
	SCAN_FIX(Messages.getString("ScanAutomation.ScanFix")); //$NON-NLS-1$
	
	/**
	 * The localized text description explaining this scanning pipeline level.
	 * 
	 * @return the descriptive localized text string.
	 */
	private final @Getter String desc;
	
	/**
	 * Checks if this automation option requires generating and storing a scan report.
	 * 
	 * @return {@code true} if a report should be built, otherwise {@code false}.
	 */
	public boolean hasReport()
	{
		return this == SCAN_REPORT || this == SCAN_REPORT_FIX || this == SCAN_REPORT_FIX_SCAN;
	}
	
	/**
	 * Checks if this automation option involves active repair / fixing operations.
	 * 
	 * @return {@code true} if files should be modified/fixed, otherwise {@code false}.
	 */
	public boolean hasFix()
	{
		return this == SCAN_FIX || this == SCAN_REPORT_FIX || this == SCAN_REPORT_FIX_SCAN;
	}
	
	/**
	 * Checks if this automation option requires performing a secondary verification scan after repairs are done.
	 * 
	 * @return {@code true} if a follow-up scan is specified, otherwise {@code false}.
	 */
	public boolean hasScanAgain()
	{
		return this == SCAN_REPORT_FIX_SCAN;
	}
}
