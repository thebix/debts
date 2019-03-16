package net.thebix.debts.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import net.thebix.debts.R;

//Диалоговое окно с подтверждением от юзера
public class ConfirmationDialogFragment extends DialogFragment {
	// Этот интерфейс должна реализовать соответствующий фрагмент, у которого
	// показывается диалог подтверждения
	public interface ConfirmationDialogListener {
		public void onConfirmationDialogPositiveClick(DialogFragment dialog,
                                                      int topicId, long itemId);
	}

	static final String KEY_TITLE = "title";
	static final String KEY_TOPIC_ID = "topicId";
	static final String KEY_ITEM_ID = "itemId"; // Иденитфикатор статьи, с которой надо произвести действия

	// Используем этот "экземпляр" для вызова методов интерфейса
	static ConfirmationDialogListener mListener;

	// titleId -- идентификатор строки с текстом
	// dialogTopicId -- идентификатор, по которому в doPositive вызывающей
	// активити определяем, какого типа действие подтверждалось
	// itemId -- если требуется, диалоговое окно вернет этот идентификатор в
	// doPositive, чтобы произвести с этой статьей действия
	static ConfirmationDialogFragment newInstance(Fragment fragment,
			int titleId, int dialogTopicId, long itemId) {
		updateListener(fragment);

		ConfirmationDialogFragment f = new ConfirmationDialogFragment();
		Bundle args = new Bundle();
		args.putInt(KEY_TITLE, titleId);
		args.putInt(KEY_TOPIC_ID, dialogTopicId);
		args.putLong(KEY_ITEM_ID, itemId);
		f.setArguments(args);
		return f;
	}

	// Инициализация "экземпляра"
	private static void updateListener(Fragment fragment) {
		try {
			mListener = (ConfirmationDialogListener) fragment;
		} catch (ClassCastException e) {
			// Активити не реализует нужый интерфейс
			throw new ClassCastException(fragment.toString()
					+ " must implement ConfirmationDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int title = getArguments().getInt(KEY_TITLE);
		final int topicId = getArguments().getInt(KEY_TOPIC_ID);
		final long itemId = getArguments().getLong(KEY_ITEM_ID);
		return new AlertDialog.Builder(getActivity())
				// .setIcon(R.drawable.ic_launcher) //INFO: можно впихнуть
				// иконку
				.setTitle(title)
				.setPositiveButton(R.string.button_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								mListener.onConfirmationDialogPositiveClick(
										ConfirmationDialogFragment.this,
										topicId, itemId);
							}
						})
				.setNegativeButton(R.string.button_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// Ничего не делаем, окно просто закроется
							}
						}).create();
	}
}
