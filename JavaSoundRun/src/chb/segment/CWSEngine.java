/**
 * 
 */
package chb.segment;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Administrator
 * 
 */
public class CWSEngine {

	public List<CWPoint> Points;
	public CWText Text;
	public String ConnectionString;
	public List<CWWord> Words;
	public List<CWWord> Merges;

	private Boolean isSegmented = false;
	private boolean isMerged;

	public Boolean IsFinished() {
		return this.isSegmented;
	}

	public CWSEngine() {
		this.Text = new CWText();
		this.Points = new LinkedList<CWPoint>();
		this.Words = new LinkedList<CWWord>();
		this.ConnectionString = "";
	}

	public CWSEngine(String text) throws Exception {
		this.Text = new CWText(text);
		this.Points = new LinkedList<CWPoint>();
		this.Words = new LinkedList<CWWord>();
		this.ConnectionString = "";
	}

	public void SetText(String text) throws Exception {
		if (this.Text == null)
			throw new Exception("Null reference CWEngine.Text");
		else
			this.Text.InitText(text);
	}

	public void Split(int size) throws Exception {
		if (this.Text == null)
			throw new Exception("Null reference CWEngine.Text");
		if (this.Points == null)
			throw new Exception("Null reference CWEngine.Points");

		CWWindow win = new CWWindow();
		win.Text = this.Text;
		win.Size = size;
		win.ConnectionString = this.ConnectionString;
		win.SetDb();

		while (win.HasNext()) {
			CWPoint pt = win.Move();
			if (pt == null) {
				break;
			}
			this.Points.add(pt.Index.intValue(), pt);
		}
	}
	
	public List<CWWord> GetMergeResult() {
		if (!this.isMerged)
			return null;
		return this.Merges;
	}

	public List<CWWord> GetMergeResult(Comparator<CWWord> icompare) {
		if (!this.isMerged)
			return null;

		Collections.sort(this.Merges, icompare);
		return this.Merges;
	}

	public void Merge(CWMerger merger) {
		
		if(merger == null)
			return;
		if(this.Words == null || this.Words.size() == 0)
			return;
		
		this.Merges = new LinkedList<CWWord>();
		
		int index = 0;
		while (index < this.Words.size()) {
			int[] range = merger.GetMergeRange(this.Words, index);
			index  = merger.Merge(this.Words, range[0], range[1], this.Merges);
		}
		
		this.isMerged = true;
	}

	public void Segmentate(CWSolver solver) throws Exception {
		
		if(solver == null)
			return;
		if(this.Points == null || this.Points.size() == 0)
			return;
		
		int index = 0;
		while (index < this.Points.size()) {
			if (IsConflicted(index)) {
				index = ConflictMove(index, solver);
			} else {
				index = NoConflictMove(index);
			}
		}

		this.isSegmented = true;
	}

	private boolean IsConflicted(int index) throws Exception {
		if (index < 0) {
			throw new Exception("Index cannot be null.");
		}

		CWPoint tpt = this.Points.get(index);

		if (!HasWord(tpt))
			return false;

		for (int i = 0; i < tpt.Segments.size(); ++i) {

			CWSegment seg = tpt.Segments.get(i);
			if (seg.Characters.size() == 1)
				continue;

			for (long j = seg.Begin + 1; j <= seg.End; ++j) {
				if (HasWord(this.Points.get((int) j)))
					return true;
			}

		}

		return false;

	}

	// / <summary>
	// / Merge segments on the condition that there is no conflict.
	// / </summary>
	// / <param name="index">The current index</param>
	// / <returns>Next index to stand on</returns>
	private int NoConflictMove(int index) throws Exception {
		if (index < 0) {
			throw new Exception("Index cannot be null.");
		}

		CWPoint tpt = this.Points.get(index);
		CWSegment seg = null;
		for (CWSegment s : tpt.Segments) {
			if (seg == null)
				seg = s;
			if (s.Characters.size() > seg.Characters.size())
				seg = s;
		}

		this.Words.add(CWWord.CreateWord(seg));

		return seg.End.intValue() + 1;
	}

	private boolean SolveConflict(int index, CWSolver solver) {
		return solver.Solve(this.Points, index);
	}

	private int ConflictMove(int index, CWSolver solver) throws Exception {
		if (!SolveConflict(index, solver))
			throw new Exception("Conflict cannot be solved at " + index);

		return NoConflictMove(index);
	}

	private boolean HasWord(CWPoint point) {
		if (point.Segments.size() == 0)
			return false;

		for (CWSegment seg : point.Segments) {
			if (seg.Characters.size() > 1)
				return true;
		}
		return false;
	}

	public List<CWWord> GetSegmentResult() {
		if (!this.isSegmented)
			return null;
		return this.Words;
	}

	public List<CWWord> GetSegmentResult(Comparator<CWWord> icompare) {
		if (!this.isSegmented)
			return null;

		Collections.sort(this.Words, icompare);
		return this.Words;
	}
}
